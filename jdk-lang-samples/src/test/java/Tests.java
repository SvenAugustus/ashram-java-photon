/**
 * @author Sven Augustus
 * @version 1.0
 */
public class Tests {

	public static void main(String[] args) {

		String[] courses = new String[]{"西红柿炒鸡蛋", "鱼香肉丝", "爆炒小龙虾"};
		String[] steps = new String[]{"放油", "倒菜", "翻炒", "出锅", "上盘"};

		for (int i = 0/* 1：初始化 */; i < courses.length /* 2：每次循环先判断*/; i++/* 10：每次循环结束一定执行的语句  ==》2*/) {
			/* 3：每次循环执行*/
			System.out.println(courses[i] + "开始炒菜...");
			/* 4: 开始内嵌套循环语句的执行 */
			for (int j = 0 /* 5：内嵌套循环初始化 */; j < steps.length/* 6：内嵌套循环每次循环先判断*/; j++/* 8：内嵌套循环每次循环结束一定执行的语句 ==》6*/) {
				/* 7：内嵌套循环每次循环执行*/
				System.out.println(courses[i] + " 步骤：" + steps[j]);
			}
			/* 9: 结束内嵌套循环语句的执行*/
			System.out.println(courses[i] + "炒菜结束");
		}
		System.out.println("全部炒菜结束");

	}

}
