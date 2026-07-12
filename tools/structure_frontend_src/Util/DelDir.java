package Util;

import java.io.File;

public class DelDir {

	public static void removeDir(File dir) {

		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				removeDir(files[i]);
			} else {
				files[i].delete();
			}
		}

		dir.delete();

		return;
	}

}
