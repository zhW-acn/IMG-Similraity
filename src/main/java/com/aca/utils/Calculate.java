package com.aca.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class Calculate {

    /**
     * 可选值为：1,2,4,8,16,32
     * 当值为64时会抛出异常，此时需要实现64位转10进制
     * radix 64 greater than Character.MAX_RADIX
     */
    public static int compareLevel = 4;

    public static void main(String[] args) throws Exception {
        Path path = Paths.get("C:\\Users\\xtkj\\Desktop\\1\\1");

        Path resPath = Paths.get("C:\\Users\\xtkj\\Desktop\\1\\res");
        Path delPath = Paths.get("C:\\Users\\xtkj\\Desktop\\1\\del");

        Double del = 0.985;


        List<File> files = new ArrayList<>();
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Path fileName = file.getFileName();
                boolean flag = true;
                File currentFile = file.toFile();
                try {
                    for (File listFileItem : files) {
                        double match = ImageHistogram.match(currentFile, listFileItem);
                        if (match >= del) { // 大于，放入del
                            try {
                                Files.copy(file, Paths.get(delPath + "\\" + fileName));
                                flag = false;
                            } catch (IOException e) {
//                                throw new RuntimeException(e);
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (flag != false) {
                    try {
                        files.add(currentFile);
                        Files.copy(file, Paths.get(resPath + "\\" + fileName));
                    } catch (IOException e) {
                    }
                }
                return super.visitFile(file, attrs);
            }
        });
    }


    public static List<Double> getPicArrayData(String path) throws Exception {
        BufferedImage image = ImageIO.read(new File(path));

        //初始化集合
        final List<Double> picFingerprint = new ArrayList<>(compareLevel * compareLevel * compareLevel);
        IntStream.range(0, compareLevel * compareLevel * compareLevel).forEach(i -> {
            picFingerprint.add(i, 0.0);
        });
        //遍历像素点
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                Color color = new Color(image.getRGB(i, j));
                //对像素点进行计算
                putIntoFingerprintList(picFingerprint, color.getRed(), color.getGreen(), color.getBlue());
            }
        }

        return picFingerprint;
    }

    /**
     * 放入像素的三原色进行计算，得到List的位置
     *
     * @param picFingerprintList picFingerprintList
     * @param r                  r
     * @param g                  g
     * @param b                  b
     * @return
     */
    public static List<Double> putIntoFingerprintList(List<Double> picFingerprintList, int r, int g, int b) throws Exception {
        //比如r g b是126, 153, 200 且 compareLevel为16进制，得到字符串：79c ,然后转10进制，这个数字就是List的位置
        final Integer index = Integer.valueOf(getBlockLocation(r) + getBlockLocation(g) + getBlockLocation(b), compareLevel);
        final Double origin = picFingerprintList.get(index);
        picFingerprintList.set(index, origin + 1);
        return picFingerprintList;
    }

    /**
     * w
     * 计算 当前原色应该分在哪个区块
     *
     * @param colorPoint colorPoint
     * @return
     */
    public static String getBlockLocation(int colorPoint) throws Exception {
        return IntStream.range(0, compareLevel)
                //以10进制计算分在哪个区块
                .filter(i -> {
                    int areaStart = (256 / compareLevel) * i;
                    int areaEnd = (256 / compareLevel) * (i + 1) - 1;
                    return colorPoint >= areaStart && colorPoint <= areaEnd;
                })
                //如果compareLevel大于10则转为对应的进制的字符串
                .mapToObj(location -> compareLevel > 10 ? Integer.toString(location, compareLevel) : location + "")
                .findFirst()
                .orElseThrow(Exception::new);
    }


}
