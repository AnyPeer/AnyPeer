package any.xxx.anypeer.util;


import java.util.Comparator;

import any.xxx.anypeer.bean.User;

public class OnlineStatusComparator implements Comparator {

	@Override
	public int compare(Object arg0, Object arg1) {
		// 按照名字排序
		User user0 = (User) arg0;
		User user1 = (User) arg1;

		if (user0.isOnline()) {
			return -1;
		} else if (user1.isOnline()) {
			return 1;
		} else {
			return 0;
		}
	}
}
