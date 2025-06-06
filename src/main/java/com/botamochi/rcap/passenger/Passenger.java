package com.botamochi.rcap.passenger;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Heightmap;
import org.mtr.core.data.Data;

import java.util.*;

public class Passenger {
    private static long NEXT_ID = 1;
    private final long id;
    public Vec3d pos;
    public Vec3d velocity = Vec3d.ZERO;
    public boolean onGround = false;

    // 歩行方向ベクトル（XZのみ。速度ではない）
    private Vec3d walkDir = Vec3d.ZERO;

    private static final double WIDTH = 0.3;
    private static final double HEIGHT = 1.6;
    private static final double JUMP_VELOCITY = 0.42;
    private static final double WALK_SPEED = 0.215;
    private static final double GRAVITY = 0.08;
    private static final double AIR_FRICTION = 0.98;
    private static final double GROUND_FRICTION = 0.6;
    private static final double ON_GROUND_THRESHOLD = 0.05;
    private static final double STEP_HEIGHT = 0.01;
    private static final double ARRIVAL_THRESHOLD = 2.0;
    private static final double EPSILON = 1e-4;

    private List<Vec3d> stationRoute = new ArrayList<>();
    private int stationRouteIndex = 0;

    private Vec3d initialTargetPos = null;
    private boolean reachedInitialTarget = false;

    // --- コンストラクタ ---
    public Passenger(BlockPos homeBlockPos, ServerWorld world) {
        this.id = NEXT_ID++;
        int x = homeBlockPos.getX();
        int y = homeBlockPos.getY();
        int z = homeBlockPos.getZ();
        this.pos = new Vec3d(x + 0.5, y + 1.0, z + 0.5);
        this.onGround = true;
    }

    public long getId() { return id; }

    public void setInitialTarget(Vec3d target) {
        this.initialTargetPos = target;
        this.reachedInitialTarget = false;
    }

    public void setStationRoute(List<Vec3d> route) {
        this.stationRoute = route;
        this.stationRouteIndex = 0;
    }

    public void tick(ServerWorld world, Data data) {
        // 落下リセット
        resetPositionToGroundIfFarBelow(world);

        // 初期目標（乗車位置）への移動
        if (initialTargetPos != null && !reachedInitialTarget) {
            if (moveTowardsTarget(world, initialTargetPos, 0.3)) {
                reachedInitialTarget = true;
            }
            return;
        }

        // 目的地（駅）への移動
        if (stationRouteIndex < stationRoute.size()) {
            Vec3d targetPos = stationRoute.get(stationRouteIndex);
            if (moveTowardsTarget(world, targetPos, ARRIVAL_THRESHOLD)) {
                stationRouteIndex++;
            }
        } else {
            // 到着後は停止
            walkDir = Vec3d.ZERO;
            velocity = Vec3d.ZERO;
            onGround = true;
        }
    }

    /**
     * targetまで歩く。到達時はtrueを返す。
     */
    private boolean moveTowardsTarget(ServerWorld world, Vec3d target, double threshold) {
        double dist = pos.distanceTo(target);
        if (dist < threshold) {
            walkDir = Vec3d.ZERO;
            velocity = new Vec3d(0, velocity.y, 0);
            return true;
        }
        Vec3d dir = target.subtract(pos);
        Vec3d flatDir = new Vec3d(dir.x, 0, dir.z);
        if (flatDir.lengthSquared() > EPSILON) {
            walkDir = flatDir.normalize();
        } else {
            walkDir = Vec3d.ZERO;
        }

        // 歩行速度をXZに反映
        Vec3d moveVec = walkDir.multiply(WALK_SPEED);
        velocity = new Vec3d(moveVec.x, velocity.y, moveVec.z);

        // 物理処理
        updateOnGround(world);
        applyGravityAndFriction();
        applyJumpIfNeeded(world);

        // 移動
        move(world);
        return false;
    }

    private void resetPositionToGroundIfFarBelow(ServerWorld world) {
        int x = MathHelper.floor(pos.x);
        int z = MathHelper.floor(pos.z);
        int bottomY = -200;
        int groundY = world.getTopY(Heightmap.Type.WORLD_SURFACE, x, z);
        if (pos.y < bottomY + 1) {
            pos = new Vec3d(x + 0.5, groundY, z + 0.5);
            velocity = Vec3d.ZERO;
            onGround = true;
        }
    }

    private void updateOnGround(ServerWorld world) {
        Box feetBox = getBoundingBox(pos).offset(0, -0.01, 0);
        VoxelShape shape = getMergedShape(world, feetBox);
        onGround = !shape.isEmpty();
        // さらに、レールやハーフブロックも地面とみなしたい場合は追加判定を
        if (!onGround) {
            BlockPos below = new BlockPos(pos.x, pos.y - 0.1, pos.z);
            BlockState state = world.getBlockState(below);
            if (state.isIn(BlockTags.RAILS)) {
                onGround = true;
            }
        }
    }

    private void applyGravityAndFriction() {
        if (!onGround) {
            velocity = velocity.add(0, -GRAVITY, 0);
            velocity = new Vec3d(velocity.x * AIR_FRICTION, velocity.y, velocity.z * AIR_FRICTION);
        } else {
            // XZのみ摩擦
            velocity = new Vec3d(velocity.x * GROUND_FRICTION, 0, velocity.z * GROUND_FRICTION);
        }
    }

    private void applyJumpIfNeeded(ServerWorld world) {
        // onGroundがtrueになった直後だけジャンプ可
        if (onGround && walkDir.lengthSquared() > EPSILON && Math.abs(velocity.y) < EPSILON && world.random.nextInt(20) == 0) {
            velocity = new Vec3d(velocity.x, JUMP_VELOCITY, velocity.z);
            onGround = false;
        }
    }

    private void move(ServerWorld world) {
        Vec3d motion = velocity;
        Box currentBox = getBoundingBox(pos);

        // Y方向
        double deltaY = tryMoveAxis(world, currentBox, motion.y, Direction.Axis.Y);
        Box movedBoxY = currentBox.offset(0, deltaY, 0);

        // X方向 + ステップアップ
        Box movedBoxXZ = moveWithStep(world, movedBoxY, motion.x, Direction.Axis.X);

        // Z方向 + ステップアップ
        Box finalBox = moveWithStep(world, movedBoxXZ, motion.z, Direction.Axis.Z);

        // 位置更新
        pos = new Vec3d(
                (finalBox.minX + finalBox.maxX) / 2,
                finalBox.minY,
                (finalBox.minZ + finalBox.maxZ) / 2
        );

        // Y方向に動けなかった＝地面にぶつかった
        if (motion.y < 0 && Math.abs(deltaY) < Math.abs(motion.y)) {
            onGround = true;
            velocity = new Vec3d(velocity.x, 0, velocity.z);
        }
    }

    private Box moveWithStep(ServerWorld world, Box box, double delta, Direction.Axis axis) {
        double move = tryMoveAxis(world, box, delta, axis);
        if (Math.abs(move) < Math.abs(delta) && Math.abs(delta) > EPSILON && onGround) {
            // ステップアップ
            Box boxStepUp = box.offset(0, STEP_HEIGHT, 0);
            double moveStepUp = tryMoveAxis(world, boxStepUp, delta, axis);
            if (Math.abs(moveStepUp) > EPSILON) {
                Box afterStep = axis == Direction.Axis.X
                        ? boxStepUp.offset(moveStepUp, 0, 0)
                        : boxStepUp.offset(0, 0, moveStepUp);
                if (getMergedShape(world, afterStep).isEmpty()) {
                    return afterStep.offset(0, -STEP_HEIGHT, 0);
                }
            }
        }
        return axis == Direction.Axis.X
                ? box.offset(move, 0, 0)
                : box.offset(0, 0, move);
    }

    private double tryMoveAxis(ServerWorld world, Box box, double delta, Direction.Axis axis) {
        if (Math.abs(delta) < EPSILON) return 0;
        double sign = Math.signum(delta);
        double remaining = Math.abs(delta);
        double actualMove = 0;
        Box current = box;
        double stepSize = 0.05;

        while (remaining > EPSILON) {
            double step = Math.min(stepSize, remaining) * sign;
            Box next = switch (axis) {
                case X -> current.offset(step, 0, 0);
                case Y -> current.offset(0, step, 0);
                case Z -> current.offset(0, 0, step);
                default -> current;
            };
            if (!getMergedShape(world, next).isEmpty()) break;
            current = next;
            actualMove += step;
            remaining -= Math.abs(step);
        }
        return actualMove;
    }

    private VoxelShape getMergedShape(ServerWorld world, Box box) {
        VoxelShape merged = VoxelShapes.empty();
        BlockPos min = new BlockPos(MathHelper.floor(box.minX), MathHelper.floor(box.minY), MathHelper.floor(box.minZ));
        BlockPos max = new BlockPos(MathHelper.ceil(box.maxX), MathHelper.ceil(box.maxY), MathHelper.ceil(box.maxZ));
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

    public boolean hasArrived() {
        return (stationRouteIndex >= stationRoute.size() && reachedInitialTarget);
    }

    private Box getBoundingBox(Vec3d pos) {
        return new Box(
                pos.x - WIDTH / 2, pos.y, pos.z - WIDTH / 2,
                pos.x + WIDTH / 2, pos.y + HEIGHT, pos.z + WIDTH / 2
        );
    }
}
