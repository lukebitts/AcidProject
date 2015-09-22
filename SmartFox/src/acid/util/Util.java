package acid.util;

import com.smartfoxserver.v2.entities.data.ISFSObject;

public class Util {

	public static final boolean DEBUG = true;

	public static boolean CheckParameters(ISFSObject data, String[] parameterNames, Class<?>[] expectedTypes) {
		assert(parameterNames.length == expectedTypes.length);

		for (int i = 0; i < parameterNames.length; ++i) {
			String parameterName = parameterNames[i];
			if (!data.containsKey(parameterName))
				return false;
			try {
				expectedTypes[i].cast(data.get(parameterName).getObject());
			} catch (ClassCastException ex) {
				return false;
			}
		}
		return true;
	}

	public static class Lock {

		boolean isLocked = false;
		Thread lockedBy = null;
		int lockedCount = 0;

		public synchronized void lock() throws InterruptedException {
			Thread callingThread = Thread.currentThread();
			while (isLocked && lockedBy != callingThread) {
				wait();
			}
			isLocked = true;
			lockedCount++;
			lockedBy = callingThread;
		}

		public synchronized void unlock() {
			if (Thread.currentThread() == this.lockedBy) {
				lockedCount--;

				if (lockedCount == 0) {
					isLocked = false;
					notify();
				}
			}
		}
	}

}
