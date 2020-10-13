package xyz.flysium.photon.algorithm.hash.basic;

import java.util.HashMap;
import java.util.Map;

/**
 * 1. 两数之和
 * <p>
 * https://leetcode-cn.com/problems/two-sum/
 *
 * @author zeno
 */
public interface T0001_TwoSum_1 {

  // 给定一个整数数组 nums 和一个目标值 target，请你在该数组中找出和为目标值的那 两个 整数，并返回他们的数组下标。
  //
  // 你可以假设每种输入只会对应一个答案。但是，数组中同一个元素不能使用两遍。

  // 2 ms 99.59%
  class Solution {

    public int[] twoSum(int[] nums, int target) {
      Map<Integer, Integer> hash = new HashMap<>(nums.length);

      // 你可以假设每种输入只会对应一个答案 -> not duplicate
      for (int i = 0; i < nums.length; i++) {
        if (hash.containsKey(target - nums[i])) {
          return new int[]{i, hash.get(target - nums[i])};
        }
        hash.put(nums[i], i);
      }
      return new int[]{};
    }

  }

}
