package xyz.flysium.photon.xalgorithm.easy;

import java.util.ArrayList;
import java.util.List;

/**
 * 1018. 可被 5 整除的二进制前缀
 * <p>
 * https://leetcode-cn.com/problems/binary-prefix-divisible-by-5/
 *
 * @author zeno
 */
public class T1018_BinaryPrefixDivisibleBy5 {

//给定由若干 0 和 1 组成的数组 A。我们定义 N_i：从 A[0] 到 A[i] 的第 i 个子数组被解释为一个二进制数（从最高有效位到最低有效位）。
//
//
// 返回布尔值列表 answer，只有当 N_i 可以被 5 整除时，答案 answer[i] 为 true，否则为 false。
//
//
//
// 示例 1：
//
// 输入：[0,1,1]
//输出：[true,false,false]
//解释：
//输入数字为 0, 01, 011；也就是十进制中的 0, 1, 3 。只有第一个数可以被 5 整除，因此 answer[0] 为真。
//
//
// 示例 2：
//
// 输入：[1,1,1]
//输出：[false,false,false]
//
//
// 示例 3：
//
// 输入：[0,1,1,1,1,1]
//输出：[true,false,false,false,true,false]
//
//
// 示例 4：
//
// 输入：[1,1,1,0,1]
//输出：[false,false,false,false,false]
//
//
//
//
// 提示：
//
//
// 1 <= A.length <= 30000
// A[i] 为 0 或 1
//
// Related Topics 数组
// 👍 55 👎 0


  public static void main(String[] args) {
    Solution solution = new T1018_BinaryPrefixDivisibleBy5().new Solution();
    System.out.println(solution.prefixesDivBy5(new int[]{0, 1, 1}));
    System.out.println(solution.prefixesDivBy5(new int[]{1, 1, 1}));
    System.out.println(solution.prefixesDivBy5(new int[]{0, 1, 1, 1, 1, 1}));
  }

  // 执行耗时:3 ms,击败了99.81% 的Java用户

  //leetcode submit region begin(Prohibit modification and deletion)
  class Solution {

    public List<Boolean> prefixesDivBy5(int[] A) {
      List<Boolean> ans = new ArrayList<>(A.length);
      int bi = 0;
      // 1 <= A.length <= 30000
      for (int a : A) {
        bi = ((bi << 1) + a) % 5;
        ans.add(bi == 0);
      }
      return ans;
    }

  }
//leetcode submit region end(Prohibit modification and deletion)


}
