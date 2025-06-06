package com.botamochi.rcap.passenger;

import com.botamochi.rcap.network.PassengerSyncPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.mtr.core.data.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * PassengerManager: 乗客管理クラス（最適化・堅牢化版）
 */
public class PassengerManager {

    /** 住宅ブロックが置かれた座標セット（重複防止＆高速化） */
    private static final Set<BlockPos> housingPositions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /** 職場ブロックが置かれた座標セット */
    private static final Set<BlockPos> officePositions = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * 乗車位置ブロックのマップ:
     * key = プラットフォームID, value = そのプラットフォームに対応する RidingPosBlock の位置リスト
     */
    private static final Map<Long, List<BlockPos>> ridingPosMap = new ConcurrentHashMap<>();

    /** アクティブな乗客リスト（スレッド安全） */
    private static final List<Passenger> activePassengers = new CopyOnWriteArrayList<>();
    private static final Random random = new Random();

    /** サーバー側の Data を渡して新規乗客を生成 */
    public static void addPassenger(Data data, World world) {
        Passenger p = createPassengerWithRoute(data, world);
        System.out.println("Adding new passenger" + p);
        if (p != null) {
            activePassengers.add(p);
            PassengerSyncPacket.sendToAll((ServerWorld) world, p, false);
        }
    }

    /** 毎サーバーティック呼ばれる。world, data を渡す。 */
    public static void tickAll(ServerWorld world, Data data) {
        final int MAX_PASSENGERS = 10;

        // 乗客数が上限未満なら、ランダムに住宅からスポーン
        if (activePassengers.size() < MAX_PASSENGERS && !housingPositions.isEmpty() && !officePositions.isEmpty()) {
            Passenger p = createPassengerWithRoute(data, world);
            if (p != null) {
                activePassengers.add(p);
                PassengerSyncPacket.sendToAll(world, p, false);
            }
        }

        // 全乗客を更新（CopyOnWriteArrayListなのでfor-eachでremove安全）
        for (Passenger p : activePassengers) {
            Vec3d oldPos = p.pos;
            p.tick(world, data);
            if (!p.pos.equals(oldPos)) {
                PassengerSyncPacket.sendToAll(world, p, false);
            }
            if (p.hasArrived()) {
                activePassengers.remove(p);
                PassengerSyncPacket.sendToAll(world, p, true);
            }
        }
    }

    /** 住宅ブロック登録 */
    public static void registerHousing(BlockPos pos) {
        housingPositions.add(pos);
    }

    /** 職場ブロック登録 */
    public static void registerOffice(BlockPos pos) {
        officePositions.add(pos);
    }

    /** 乗車位置ブロック登録 */
    public static void registerRidingPos(Long platformId, BlockPos pos) {
        // CopyOnWriteArrayListでスレッド安全
        ridingPosMap.computeIfAbsent(platformId, k -> new CopyOnWriteArrayList<>()).add(pos);
    }

    /** 乗客を作成するヘルパーメソッド */
    private static Passenger createPassengerWithRoute(Data data, World world) {
        if (housingPositions.isEmpty() || officePositions.isEmpty()) return null;

        // 住宅と職場をランダムに選ぶ
        List<BlockPos> housingList = new ArrayList<>(housingPositions);
        List<BlockPos> officeList = new ArrayList<>(officePositions);
        BlockPos home = housingList.get(random.nextInt(housingList.size()));
        BlockPos office = officeList.get(random.nextInt(officeList.size()));

        // 最寄り駅を探す
        Station homeStation = findNearestStation(data, home);
        Station officeStation = findNearestStation(data, office);

        if (homeStation == null || officeStation == null) return null;

        // プラットフォーム一覧から「ホーム駅に対応するプラットフォーム」を探す
        Optional<Platform> departurePlatformOpt = data.platforms.stream()
                .filter(p -> p.area instanceof Station && ((Station) p.area).getId() == homeStation.getId())
                .findFirst();

        Optional<Platform> arrivalPlatformOpt = data.platforms.stream()
                .filter(p -> p.area instanceof Station && ((Station) p.area).getId() == officeStation.getId())
                .findFirst();

        if (departurePlatformOpt.isEmpty() || arrivalPlatformOpt.isEmpty()) return null;

        Platform departurePlatform = departurePlatformOpt.get();
        Platform arrivalPlatform = arrivalPlatformOpt.get();

        // プラットフォーム間の経路を求める (Platform ID のリスト)
        List<Long> platformRoute = PassengerRouteFinder.findRoute(data, departurePlatform.getId(), arrivalPlatform.getId());
        if (platformRoute.isEmpty()) return null;

        // 乗車位置ブロックをランダム選択
        BlockPos ridingPos = chooseRandomRidingPos(departurePlatform.getId());
        if (ridingPos == null) return null;

        // Passenger インスタンスを生成
        Passenger passenger = new Passenger(home, (ServerWorld) world);

        // 最初に「乗車ブロック」(RidingPosBlock) を目標にする
        Vec3d firstTarget = new Vec3d(ridingPos.getX() + 0.5, ridingPos.getY(), ridingPos.getZ() + 0.5);
        passenger.setInitialTarget(firstTarget);

        // 経路上の「駅中心座標」をリスト化して Passenger に伝える
        List<Vec3d> stationCenters = new ArrayList<>();
        for (Long platId : platformRoute) {
            data.platforms.stream()
                    .filter(p -> p.getId() == platId && p.area instanceof Station)
                    .findFirst()
                    .ifPresent(plat -> {
                        Station st = (Station) plat.area;
                        int midX = MathHelper.floor(st.getCenter().getX());
                        int midZ = MathHelper.floor(st.getCenter().getZ());
                        int groundY = world.getTopY(Heightmap.Type.WORLD_SURFACE, midX, midZ);
                        stationCenters.add(new Vec3d(midX + 0.5, groundY, midZ + 0.5));
                    });
        }
        passenger.setStationRoute(stationCenters);
        System.out.println("Passenger created: " + passenger + " with route: " + platformRoute + " from " + home + " to " + office);

        return passenger;
    }

    /** 最寄り駅を見つけるユーティリティ */
    private static Station findNearestStation(Data data, BlockPos blockPos) {
        if (data.stations == null || data.stations.isEmpty()) return null;

        double minDist = Double.MAX_VALUE;
        Station nearest = null;
        double x = blockPos.getX() + 0.5;
        double z = blockPos.getZ() + 0.5;

        for (Station s : data.stations) {
            double sx = s.getCenter().getX();
            double sz = s.getCenter().getZ();
            double dist = Math.hypot(sx - x, sz - z);
            if (dist < minDist) {
                minDist = dist;
                nearest = s;
            }
        }
        return nearest;
    }

    /** プラットフォームID に対応する RidingPosBlock の中からランダムで1つ返す */
    private static BlockPos chooseRandomRidingPos(long departurePlatformId) {
        List<BlockPos> list = ridingPosMap.get(departurePlatformId);
        if (list == null || list.isEmpty()) return null;
        return list.get(random.nextInt(list.size()));
    }

    public static List<Passenger> getPassengers() {
        return new ArrayList<>(activePassengers); // CopyOnWriteArrayListなので同期不要
    }
}
