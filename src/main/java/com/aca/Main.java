package com.aca;

import com.aca.utils.ImageHistogram;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * @Description:
 * @Author: aca
 * @Date: 2024/08/08
 */
public class Main {

    // 相似度大于该值时删除
    private static final double DEL_THRESHOLD = 0.985;
    // 保留文件的目录
    private static final Path RES_PATH = Paths.get("");
    // 重复的图片目录
    private static final Path DEL_PATH = Paths.get("");
    // 被筛选的目录
    private static final Path SOU_PATH = Paths.get("C:\\Users\\xtkj\\Desktop\\pic\\source");


    private static final ExecutorService filePool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static final LinkedBlockingQueue<File> fileList = new LinkedBlockingQueue<>();
    private static final ReentrantReadWriteLock fileLock = new ReentrantReadWriteLock(true);


    private static final ExecutorService resMovePool = Executors.newFixedThreadPool(1);
    private static final LinkedBlockingQueue<File> resMoveList = new LinkedBlockingQueue<>();
    private static final ReentrantReadWriteLock resLock = new ReentrantReadWriteLock(true);

    private static final LinkedBlockingQueue<File> delMoveList = new LinkedBlockingQueue<>();
    private static final ExecutorService delMovePool = Executors.newFixedThreadPool(1);
    private static final ReentrantReadWriteLock delLock = new ReentrantReadWriteLock(true);

    private final static AtomicInteger atomicInteger = new AtomicInteger();

    private static final Map<File, float[]> fileMap = new ConcurrentHashMap<>();

    public static Runnable resMoveRunner = new Runnable() {
        @Override
        public void run() {
            while (true) {
                resLock.writeLock().lock();
                try {
                    File file = resMoveList.take();
                    Files.copy(file.toPath(), RES_PATH.resolve(file.toPath().getFileName()));
                    System.out.println("保留，还剩" + resMoveList.size());
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    resLock.writeLock().unlock();
                }
            }
        }
    };

    public static Runnable delMoveRunner = new Runnable() {
        @Override
        public void run() {
            while (true) {
                delLock.writeLock().lock();
                try {
                    File file = delMoveList.take();
                    Files.copy(file.toPath(), DEL_PATH.resolve(file.toPath().getFileName()));
                    System.out.println("删除，还剩" + delMoveList.size());
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    delLock.writeLock().unlock();
                }
            }
        }
    };
    public static Runnable fileRunner = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    fileLock.readLock().lock();
                    File file = fileList.take();
                    processFile(file);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    fileLock.readLock().unlock();
                    System.out.println("文件，还剩" + fileList.size());
                }
            }
        }
    };

    public static void main(String[] args) throws IOException {
        resMovePool.submit(resMoveRunner);
        delMovePool.submit(delMoveRunner);
        for (int i = 0; i < 10; i++) {
            filePool.submit(fileRunner);
        }

        Files.walkFileTree(SOU_PATH, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                fileList.add(file.toFile());
                System.out.println(atomicInteger.addAndGet(1));
                return super.visitFile(file, attrs);
            }
        });

    }


    private static void processFile(File file) {
        try {
            boolean flag = false;
            float[] fileFloats = ImageHistogram.returnFloatArray(file);
            for (Map.Entry<File, float[]> entry : fileMap.entrySet()) {
                double match = ImageHistogram.calcSimilarity(fileFloats, entry.getValue());
                if (match >= DEL_THRESHOLD) {
                    delMoveList.add(file);
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                fileMap.put(file, fileFloats);
                resMoveList.add(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("已存文件：" + fileMap.size());
        }
    }


}