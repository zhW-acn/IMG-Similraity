package com.aca.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 皮尔逊相关系数
 */
public class PearsonDemo {
    public static Double getPearsonBydim(List<Double> ratingOne, List<Double> ratingTwo) {
        try {
            if(ratingOne.size() != ratingTwo.size()) {//两个变量的观测值是成对的，每对观测值之间相互独立。
                if(ratingOne.size() > ratingTwo.size()) {//保留小的处理大
                    List<Double> temp = ratingOne;
                    ratingOne = new ArrayList<>();
                    for(int i=0;i<ratingTwo.size();i++) {
                        ratingOne.add(temp.get(i));
                    }
                }else {
                    List<Double> temp = ratingTwo;
                    ratingTwo = new ArrayList<>();
                    for(int i=0;i<ratingOne.size();i++) {
                        ratingTwo.add(temp.get(i));
                    }
                }
            }
            double sim = 0D;//最后的皮尔逊相关度系数
            double commonItemsLen = ratingOne.size();//操作数的个数
            double oneSum = 0D;//第一个相关数的和
            double twoSum = 0D;//第二个相关数的和
            double oneSqSum = 0D;//第一个相关数的平方和
            double twoSqSum = 0D;//第二个相关数的平方和
            double oneTwoSum = 0D;//两个相关数的乘积和
            for(int i=0;i<ratingOne.size();i++) {//计算
                double oneTemp = ratingOne.get(i);
                double twoTemp = ratingTwo.get(i);
                //求和
                oneSum += oneTemp;
                twoSum += twoTemp;
                oneSqSum += Math.pow(oneTemp, 2);
                twoSqSum += Math.pow(twoTemp, 2);
                oneTwoSum += oneTemp*twoTemp;
            }
            double num = (commonItemsLen*oneTwoSum) - (oneSum*twoSum);
            double den = Math.sqrt((commonItemsLen * oneSqSum - Math.pow(oneSum, 2)) * (commonItemsLen * twoSqSum - Math.pow(twoSum, 2)));
            sim = (den == 0) ? 1 : num / den;
            return sim;
        } catch (Exception e) {
            return null;
        }
    }
}


