package com.botamochi.rcap.passenger;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Heightmap;
import org.mtr.core.data.Data;
import org.mtr.core.data.Station;

import java.util.*;

public class Passenger {
    // 現在位置と速度ベクトル
    public Vec3d pos;
    public Vec3d velocity = Vec3d.ZERO;
    public boolean onGround = false;

    // 歩行方向
    private Vec3d walkDir = Vec3d.ZERO;

    // 定数定義
    private static final double WIDTH = 0.3; // 乗客の幅（直径）
    private static final double HEIGHT = 1.6; // 乗客の高さ
    private static final double JUMP_VELOCITY = 0.42; // ジャンプ時のY速度
    private static final double WALK_SPEED = 0.215; // ★修正点: プレイヤーの通常の歩行速度に調整
    private static final double ARRIVAL_THRESHOLD = 2.0; // 目的地への到着判定距離
    private static final double GRAVITY = 0.08; // 重力加速度
    private static final double AIR_FRICTION = 0.98; // 空中での摩擦（速度減衰）
    private static final double ON_GROUND_THRESHOLD = 0.05; // ★調整: 地面判定のための僅かなオフセット
    private static final double STEP_HEIGHT = 0.6; // ★調整: プレイヤーが乗り越えられる段差の高さ (0.6はMCのプレイヤーが登れる最大)

    // 目的地
    private Vec3d targetPos = null;
    // 経路上の次の経由地点リスト
    private List<BlockPos> path = new ArrayList<>();
    // 前回のパスファインディングの開始ブロック座標
    private BlockPos pathfindingStartBlock = null;
    // パスファインディングを再計算するクールダウン
    private int pathRecalculateCooldown = 0;
    private static final int PATH_RECALCULATE_INTERVAL = 40; // 2秒 (40ティック) ごとに経路を再計算

    public Passenger(Vec3d initialPos) {
        this.pos = initialPos;
    }

    public void tick(ServerWorld world, Data data) {
        // ① 奈落に落ちた場合のみ地面にリセット
        resetPositionToGroundIfFarBelow(world);

        // 目的地設定
        findNearestStationIfNeeded(world, data);

        // パスファインディングと歩行方向の更新
        if (targetPos != null) {
            // 現在のブロック座標を取得（切り捨てで位置合わせ）
            BlockPos currentBlock = new BlockPos(
                    (int) Math.floor(pos.x),
                    (int) Math.floor(pos.y),
                    (int) Math.floor(pos.z)
            );
            BlockPos targetBlock = new BlockPos(
                    (int) Math.floor(targetPos.x),
                    (int) Math.floor(targetPos.y),
                    (int) Math.floor(targetPos.z)
            );

            // ★調整: 経路再計算の条件
            if (pathfindingStartBlock == null || !currentBlock.equals(pathfindingStartBlock) || pathRecalculateCooldown <= 0 || path.isEmpty()) {
                path = findPath(world, currentBlock, targetBlock);
                pathfindingStartBlock = currentBlock;
                pathRecalculateCooldown = PATH_RECALCULATE_INTERVAL;
            } else {
                pathRecalculateCooldown--;
            }

            if (!path.isEmpty()) {
                BlockPos nextPathNode = path.get(0);
                Vec3d nextCenter = new Vec3d(nextPathNode.getX() + 0.5, nextPathNode.getY(), nextPathNode.getZ() + 0.5);
                updateWalkDirectionTowards(nextCenter);

                // 到着判定: 次の経路ノードに十分近づいたら削除
                if (pos.distanceTo(nextCenter) < 0.3 && Math.abs(pos.y - nextCenter.y) < 1.0) {
                    path.remove(0);
                }
            } else {
                // 経路が見つからない場合、直接目的地に向かう（障害物回避なし）
                updateWalkDirectionTowards(targetPos);
            }
        } else {
            walkDir = Vec3d.ZERO; // 目的地がない場合は停止
        }

        // 重力・接地判定・摩擦
        updateOnGround(world); // onGroundを更新
        applyGravityAndFriction(); // velocityを更新 (onGroundに基づいてY速度を0にしたり摩擦を適用)
        applyJumpIfNeeded(world); // ジャンプを適用 (velocityを更新)

        // 移動
        move(world);
    }

    /**
     * 地下深く（ワールド底より1マス）に落ちた場合のみ、地面の高さにリセットする
     */
    private void resetPositionToGroundIfFarBelow(ServerWorld world) {
        int x = MathHelper.floor(pos.x);
        int z = MathHelper.floor(pos.z);
        int groundY = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
        int bottomY = world.getBottomY();
        // 地下深くまで落ちた場合のみリセット
        if (pos.y < bottomY + 1) {
            pos = new Vec3d(pos.x, groundY, pos.z);
            velocity = Vec3d.ZERO;
        }
    }

    private void findNearestStationIfNeeded(ServerWorld world, Data data) {
        if (targetPos != null && pos.distanceTo(targetPos) > ARRIVAL_THRESHOLD) {
            return;
        }
        if (targetPos != null && pos.distanceTo(targetPos) <= ARRIVAL_THRESHOLD) {
            targetPos = null; // 到着したので目的地をクリア
            path.clear(); // 経路もクリア
            pathfindingStartBlock = null; // パスファインディングの開始点もクリア
        }
        double minDistance = Double.MAX_VALUE;
        Vec3d nearest = null;
        if (data.stations != null) {
            for (Station station : data.stations) {
                int midX = (int) station.getCenter().getX();
                int midZ = (int) station.getCenter().getZ();
                // 地面のYを取得して踏み台を地上に設定
                int groundY = world.getTopY(Heightmap.Type.WORLD_SURFACE, midX, midZ);
                Vec3d stationCenter = new Vec3d(midX + 0.5, groundY, midZ + 0.5);
                double dist = pos.distanceTo(stationCenter);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearest = stationCenter;
                }
            }
        }
        if (nearest != null) {
            if (targetPos == null || !nearest.equals(targetPos)) {
                targetPos = nearest;
                path.clear();
                pathfindingStartBlock = null;
            }
        }
    }

    private void updateWalkDirectionTowards(Vec3d destination) {
        Vec3d dir = destination.subtract(pos);
        Vec3d flatDir = new Vec3d(dir.x, 0, dir.z);
        if (flatDir.lengthSquared() > 0.01) {
            walkDir = flatDir.normalize().multiply(WALK_SPEED);
        } else {
            walkDir = Vec3d.ZERO;
        }
        // velocity.y はそのまま保持
    }

    private void updateOnGround(ServerWorld world) {
        Box box = getBoundingBox(pos);
        Box boxBelow = box.offset(0, -ON_GROUND_THRESHOLD, 0);
        VoxelShape shapeBelow = getMergedShape(world, boxBelow);
        onGround = !shapeBelow.isEmpty();
        if (onGround && velocity.y < 0) {
            velocity = new Vec3d(velocity.x, 0, velocity.z);
        }
    }

    private void applyGravityAndFriction() {
        if (!onGround) {
            velocity = velocity.add(0, -GRAVITY, 0);
            velocity = new Vec3d(velocity.x * AIR_FRICTION, velocity.y, velocity.z * AIR_FRICTION);
        } else {
            velocity = new Vec3d(velocity.x * 0.6, 0, velocity.z * 0.6);
        }
    }

    private void applyJumpIfNeeded(ServerWorld world) {
        if (onGround && walkDir.lengthSquared() > 0 && world.random.nextInt(20) == 0 && velocity.y == 0) {
            velocity = new Vec3d(velocity.x, JUMP_VELOCITY, velocity.z);
            onGround = false;
        }
    }

    private void move(ServerWorld world) {
        Vec3d motion = new Vec3d(walkDir.x, velocity.y, walkDir.z);
        Box currentBox = getBoundingBox(pos);

        // Y移動
        double deltaY = tryMoveAxis(world, currentBox, motion.y, Direction.Axis.Y);
        Box movedBoxY = currentBox.offset(0, deltaY, 0);
        if (motion.y > 0 && deltaY < motion.y) {
            velocity = new Vec3d(velocity.x, 0, velocity.z);
        }

        // X移動 + ステップアップ
        double deltaX = tryMoveAxis(world, movedBoxY, motion.x, Direction.Axis.X);
        Box movedBoxXZ = movedBoxY;
        if (Math.abs(deltaX) < Math.abs(motion.x) && motion.x != 0 && onGround) {
            Box boxStepUp = movedBoxY.offset(0, STEP_HEIGHT, 0);
            double deltaXStepUp = tryMoveAxis(world, boxStepUp, motion.x, Direction.Axis.X);
            if (Math.abs(deltaXStepUp) > 1e-6) {
                Box afterStepAttempt = boxStepUp.offset(deltaXStepUp, 0, 0);
                if (getMergedShape(world, afterStepAttempt).isEmpty()) {
                    movedBoxXZ = afterStepAttempt.offset(0, -STEP_HEIGHT, 0);
                }
            }
        } else {
            movedBoxXZ = movedBoxY.offset(deltaX, 0, 0);
        }

        // Z移動 + ステップアップ
        double deltaZ = tryMoveAxis(world, movedBoxXZ, motion.z, Direction.Axis.Z);
        Box finalBox = movedBoxXZ.offset(0, 0, deltaZ);
        if (Math.abs(deltaZ) < Math.abs(motion.z) && motion.z != 0 && onGround) {
            Box boxStepUp = movedBoxXZ.offset(0, STEP_HEIGHT, 0);
            double deltaZStepUp = tryMoveAxis(world, boxStepUp, motion.z, Direction.Axis.Z);
            if (Math.abs(deltaZStepUp) > 1e-6) {
                Box afterStepAttempt = boxStepUp.offset(0, 0, deltaZStepUp);
                if (getMergedShape(world, afterStepAttempt).isEmpty()) {
                    finalBox = afterStepAttempt.offset(0, -STEP_HEIGHT, 0);
                }
            }
        }

        pos = new Vec3d(
                (finalBox.minX + finalBox.maxX) / 2,
                finalBox.minY,
                (finalBox.minZ + finalBox.maxZ) / 2
        );
    }

    private List<BlockPos> findPath(ServerWorld world, BlockPos start, BlockPos goal) {
        Set<BlockPos> closed = new HashSet<>();
        PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(n -> n.f));
        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
        Map<BlockPos, Double> gScore = new HashMap<>();
        gScore.put(start, 0.0);
        open.add(new Node(start, heuristic(start, goal)));

        int maxIterations = 2000;
        int currentIterations = 0;

        while (!open.isEmpty() && currentIterations < maxIterations) {
            currentIterations++;
            Node current = open.poll();
            if (current.pos.equals(goal)) {
                return reconstructPath(cameFrom, current.pos);
            }
            closed.add(current.pos);
            for (BlockPos neighbor : getNeighbors(world, current.pos)) {
                if (closed.contains(neighbor)) continue;
                double tentativeG = gScore.getOrDefault(current.pos, Double.MAX_VALUE) + 1;
                if (tentativeG < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, current.pos);
                    gScore.put(neighbor, tentativeG);
                    double f = tentativeG + heuristic(neighbor, goal);
                    open.add(new Node(neighbor, f));
                }
            }
        }
        return new ArrayList<>();
    }

    private List<BlockPos> reconstructPath(Map<BlockPos, BlockPos> cameFrom, BlockPos current) {
        List<BlockPos> totalPath = new ArrayList<>();
        totalPath.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            totalPath.add(0, current);
        }
        return totalPath;
    }

    private double heuristic(BlockPos a, BlockPos b) {
        return a.getManhattanDistance(b);
    }

    private List<BlockPos> getNeighbors(ServerWorld world, BlockPos pos) {
        List<BlockPos> neighbors = new ArrayList<>();
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : dirs) {
            BlockPos np = pos.add(d[0], 0, d[1]);
            if (isWalkable(world, np, pos.getY())) {
                neighbors.add(new BlockPos(np.getX(), pos.getY(), np.getZ()));
            }
            if (isWalkable(world, np, pos.getY() + 1)) {
                neighbors.add(new BlockPos(np.getX(), pos.getY() + 1, np.getZ()));
            }
            if (isWalkable(world, np, pos.getY() - 1)) {
                neighbors.add(new BlockPos(np.getX(), pos.getY() - 1, np.getZ()));
            }
        }
        return neighbors;
    }

    private boolean isWalkable(ServerWorld world, BlockPos testBlockPos, int targetYLevel) {
        Box passengerSpace = new Box(
                testBlockPos.getX() + 0.5 - WIDTH / 2,
                targetYLevel,
                testBlockPos.getZ() + 0.5 - WIDTH / 2,
                testBlockPos.getX() + 0.5 + WIDTH / 2,
                targetYLevel + HEIGHT,
                testBlockPos.getZ() + 0.5 + WIDTH / 2
        );
        Box groundCheckArea = new Box(
                passengerSpace.minX,
                passengerSpace.minY - ON_GROUND_THRESHOLD,
                passengerSpace.minZ,
                passengerSpace.maxX,
                passengerSpace.minY,
                passengerSpace.maxZ
        );
        if (getMergedShape(world, groundCheckArea).isEmpty()) {
            return false;
        }
        if (!getMergedShape(world, passengerSpace).isEmpty()) {
            return false;
        }
        return true;
    }

    private double tryMoveAxis(ServerWorld world, Box box, double delta, Direction.Axis axis) {
        if (Math.abs(delta) < 1e-6) return 0;
        double sign = Math.signum(delta);
        double remaining = Math.abs(delta);
        double actualMove = 0;
        Box currentIterationBox = box;
        double stepSize = 0.05;
        while (remaining > 1e-4) {
            double step = Math.min(stepSize, remaining) * sign;
            Box nextBox;
            switch (axis) {
                case X -> nextBox = currentIterationBox.offset(step, 0, 0);
                case Y -> nextBox = currentIterationBox.offset(0, step, 0);
                case Z -> nextBox = currentIterationBox.offset(0, 0, step);
                default -> nextBox = currentIterationBox;
            }
            if (!getMergedShape(world, nextBox).isEmpty()) {
                break;
            }
            currentIterationBox = nextBox;
            actualMove += step;
            remaining -= Math.abs(step);
        }
        return actualMove;
    }

    private VoxelShape getMergedShape(ServerWorld world, Box box) {
        VoxelShape merged = VoxelShapes.empty();
        BlockPos min = new BlockPos(
                MathHelper.floor(box.minX),
                MathHelper.floor(box.minY),
                MathHelper.floor(box.minZ)
        );
        BlockPos max = new BlockPos(
                MathHelper.ceil(box.maxX),
                MathHelper.ceil(box.maxY),
                MathHelper.ceil(box.maxZ)
        );
        for (int x = min.getX(); x < max.getX(); x++) {
            for (int y = min.getY(); y < max.getY(); y++) {
                for (int z = min.getZ(); z < max.getZ(); z++) {
                    BlockPos p = new BlockPos(x, y, z);
                    VoxelShape shape = world.getBlockState(p).getCollisionShape(world, p);
                    if (!shape.isEmpty()) {
                        merged = VoxelShapes.union(merged, shape.offset(x, y, z));
                    }
                }
            }
        }
        return merged;
    }

    private Box getBoundingBox(Vec3d pos) {
        return new Box(
                pos.x - WIDTH / 2, pos.y, pos.z - WIDTH / 2,
                pos.x + WIDTH / 2, pos.y + HEIGHT, pos.z + WIDTH / 2
        );
    }

    private static class Node {
        BlockPos pos;
        double f;
        public Node(BlockPos pos, double f) {
            this.pos = pos;
            this.f = f;
        }
    }
}
