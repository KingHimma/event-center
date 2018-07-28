package eventcenter.leveldb.utils;

import eventcenter.leveldb.LevelDBPersistenceAdapter;

import java.io.File;

/**
 * @Author: ruanyaguang
 * @Date : 18/1/3
 */
public class LevelDbUtils {
    /**
     * 获取leveldb中sst文件的统计信息
     *
     * @param adapter
     * @return
     */
    public static SstFileStatisticsInfo generateSstFileStatisticsInfo(LevelDBPersistenceAdapter adapter) {
        File dirPath = adapter.getDirPath();
        int sstFileNum = 0;
        long sstFileSize = 0;
        for (File file : dirPath.listFiles()) {
            if (file.getName().endsWith(".sst")) {
                sstFileNum++;
                sstFileSize += file.length();
            }
        }
        SstFileStatisticsInfo info = new SstFileStatisticsInfo();
        info.setFileNum(sstFileNum);
        info.setFileSize(sstFileSize);
        return info;
    }

    public static class SstFileStatisticsInfo {
        // sst文件总数量
        private int fileNum;

        // sst文件总大小（单位：byte）
        private long fileSize;

        public int getFileNum() {
            return fileNum;
        }

        public void setFileNum(int fileNum) {
            this.fileNum = fileNum;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        @Override
        public String toString() {
            return "SstFileStatisticsInfo{" +
                "fileNum=" + fileNum +
                ", fileSize=" + fileSize +
                '}';
        }
    }

    private LevelDbUtils() {
    }
}
