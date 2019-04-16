/************************************************************************************************
 *  _________          _     ____          _           __        __    _ _      _   _   _ ___
 * |__  / ___|__ _ ___| |__ / ___|_      _(_)_ __   __ \ \      / /_ _| | | ___| |_| | | |_ _|
 *   / / |   / _` / __| '_ \\___ \ \ /\ / / | '_ \ / _` \ \ /\ / / _` | | |/ _ \ __| | | || |
 *  / /| |__| (_| \__ \ | | |___) \ V  V /| | | | | (_| |\ V  V / (_| | | |  __/ |_| |_| || |
 * /____\____\__,_|___/_| |_|____/ \_/\_/ |_|_| |_|\__, | \_/\_/ \__,_|_|_|\___|\__|\___/|___|
 *                                                 |___/
 *
 * Copyright (c) 2016 Ivan Vaklinov <ivan@vaklinov.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 **********************************************************************************/
package com.vaklinov.vectoriumwallet;


import java.io.File;
import java.io.IOException;
import java.util.Locale;


/**
 * Utilities - may be OS dependent.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class OSUtil
{

	public static enum OS_TYPE
	{
		LINUX, WINDOWS, MAC_OS, FREE_BSD, OTHER_BSD, SOLARIS, AIX, OTHER_UNIX, OTHER_OS
	};
	
	
	public static boolean isUnixLike(OS_TYPE os)
	{
		return os == OS_TYPE.LINUX || os == OS_TYPE.MAC_OS || os == OS_TYPE.FREE_BSD || 
			   os == OS_TYPE.OTHER_BSD || os == OS_TYPE.SOLARIS || os == OS_TYPE.AIX || 
			   os == OS_TYPE.OTHER_UNIX;
	}
	
	
	public static boolean isHardUnix(OS_TYPE os)
	{
		return os == OS_TYPE.FREE_BSD || 
			   os == OS_TYPE.OTHER_BSD || os == OS_TYPE.SOLARIS || 
			   os == OS_TYPE.AIX || os == OS_TYPE.OTHER_UNIX;
	}
	
	
	public static OS_TYPE getOSType()
	{
		String name = System.getProperty("os.name").toLowerCase(Locale.ROOT);
		
		if (name.contains("linux"))
		{
			return OS_TYPE.LINUX;
		} else if (name.contains("windows"))
		{
			return OS_TYPE.WINDOWS;
		} else if (name.contains("sunos") || name.contains("solaris"))
		{
			return OS_TYPE.SOLARIS;
		} else if (name.contains("darwin") || name.contains("mac os") || name.contains("macos"))
		{
			return OS_TYPE.MAC_OS;
		} else if (name.contains("free") && name.contains("bsd"))
		{
			return OS_TYPE.FREE_BSD;
		} else if ((name.contains("open") || name.contains("net")) && name.contains("bsd"))
		{
			return OS_TYPE.OTHER_BSD;
		} else if (name.contains("aix"))
		{
			return OS_TYPE.AIX;
		} else if (name.contains("unix"))
		{
			return OS_TYPE.OTHER_UNIX;
		} else
		{
			return OS_TYPE.OTHER_OS;
		}
	}
	
	
	// Returns the name of the vectoriumd server - may vary depending on the OS.
	public static String getVectoriumd()
	{
		String vectoriumd = "vectoriumd";
		
		OS_TYPE os = getOSType();
		if (os == OS_TYPE.WINDOWS)
		{
			vectoriumd += ".exe";
		}
		
		return vectoriumd;
	}
	
	
	// Returns the name of the vectorium-cli tool - may vary depending on the OS.
	public static String getVectoriumCli()
	{
		String vectoriumcli = "vectorium-cli";
		
		OS_TYPE os = getOSType();
		if (os == OS_TYPE.WINDOWS)
		{
			vectoriumcli += ".exe";
		}
		
		return vectoriumcli;
	}


	// Returns the directory that the wallet program was started from
	public static String getProgramDirectory()
		throws IOException
	{
		// TODO: this way of finding the dir is JAR name dependent - tricky, may not work
		// if program is repackaged as different JAR!
		final String JAR_NAME = "VECTSwingWalletUI.jar";
		String cp = System.getProperty("java.class.path");
		if ((cp != null) && (cp.indexOf(File.pathSeparator) == -1) &&
			(cp.endsWith(JAR_NAME)))
		{
			File pd = new File(cp.substring(0, cp.length() - JAR_NAME.length()));

			if (pd.exists() && pd.isDirectory())
			{
				return pd.getCanonicalPath();
			}
		}

		// Current dir of the running JVM (expected)
		String userDir = System.getProperty("user.dir");
		if (userDir != null)
		{
			File ud = new File(userDir);

			if (ud.exists() && ud.isDirectory())
			{
				return ud.getCanonicalPath();
			}
		}

		// TODO: tests and more options

		return new File(".").getCanonicalPath();
	}
	
	
	public static File getUserHomeDirectory()
		throws IOException
	{
        return new File(System.getProperty("user.home"));
	}


	public static String getBlockchainDirectory()
		throws IOException
	{
		OS_TYPE os = getOSType();
		
		if (os == OS_TYPE.MAC_OS)
		{
			return new File(System.getProperty("user.home") + "/Library/Application Support/vectorium").getCanonicalPath();
		} else if (os == OS_TYPE.WINDOWS)
		{
			return new File(System.getenv("APPDATA") + "\\Vectorium").getCanonicalPath();
		} else
		{
			return new File(System.getProperty("user.home") + "/.vectorium").getCanonicalPath();
		}
	}


	// Directory with program settings to store as well as logging
	public static String getSettingsDirectory()
		throws IOException
	{
	    File userHome = new File(System.getProperty("user.home"));
	    File dir;
	    OS_TYPE os = getOSType();
	    
	    if (os == OS_TYPE.MAC_OS)
	    {
	        dir = new File(userHome, "Library/Application Support/VectoriumSwingWalletUI");
	    } else if (os == OS_TYPE.WINDOWS)
		{
			dir = new File(System.getenv("LOCALAPPDATA") + "\\VectoriumSwingWalletUI");
		} else
	    {
	        dir = new File(userHome.getCanonicalPath() + File.separator + ".VectoriumSwingWalletUI");
	    }
	    
		if (!dir.exists())
		{
			if (!dir.mkdirs())
			{
				Log.warning("Could not create settings directory: " + dir.getCanonicalPath());
			}
		}

		return dir.getCanonicalPath();
	}


	public static String getSystemInfo()
		throws IOException, InterruptedException
	{
		OS_TYPE os = getOSType();
		
		if (os == OS_TYPE.MAC_OS)
		{
			CommandExecutor uname = new CommandExecutor(new String[] { "uname", "-sr" });
		    return uname.execute() + "; " + 
		           System.getProperty("os.name") + " " + System.getProperty("os.version");
		} else if (os == OS_TYPE.WINDOWS)
		{
			// TODO: More detailed Windows information
			return System.getProperty("os.name");
		} else
		{
			CommandExecutor uname = new CommandExecutor(new String[] { "uname", "-srv" });
		    return uname.execute();
		}
	}


	// Can be used to find vectoriumd/vectorium-cli if it is not found in the same place as the wallet JAR
	// Null if not found
	public static File findVectoriumCommand(String command)
		throws IOException
	{
	    File f;
	    
	    // Try with system property vectorium.location.dir - may be specified by caller
	    String VectoriumLocationDir = System.getProperty("vectorium.location.dir");
	    if ((VectoriumLocationDir != null) && (VectoriumLocationDir.trim().length() > 0))
	    {
	        f = new File(VectoriumLocationDir + File.separator + command);
	        if (f.exists() && f.isFile())
	        {
	            return f.getCanonicalFile();
	        }
	    }
	    
	    OS_TYPE os = getOSType();
	    
	    if (isUnixLike(os))
	    {
	    	// The following search directories apply to UNIX-like systems only
			final String dirs[] = new String[]
			{
				"/usr/bin/", // Typical Ubuntu
				"/bin/",
				"/usr/local/bin/",
				"/usr/local/vectorium/bin/",
				"/usr/lib/vectorium/bin/",
				"/opt/local/bin/",
				"/opt/local/vectorium/bin/",
				"/opt/vectorium/bin/"
			};
	
			for (String d : dirs)
			{
				f = new File(d + command);
				if (f.exists())
				{
					return f;
				}
			}
			
	    } else if (os == OS_TYPE.WINDOWS)
	    {
	    	// A probable Windows directory is a Vectorium dir in Program Files
	    	String programFiles = System.getenv("PROGRAMFILES");
	    	if ((programFiles != null) && (!programFiles.isEmpty()))
	    	{
	    		File pf = new File(programFiles);
	    		if (pf.exists() && pf.isDirectory())
	    		{
	    			File ZDir = new File(pf, "Vectorium");
	    			if (ZDir.exists() && ZDir.isDirectory())
	    			{
	    				File cf = new File(ZDir, command);
	    				if (cf.exists() && cf.isFile())
	    				{
	    					return cf;
	    				}
	    			}
	    		}
	    	}
	    }
		
		// Try in the current directory
		f = new File("." + File.separator + command);
		if (f.exists() && f.isFile())
		{
			return f.getCanonicalFile();
		}
			

		// TODO: Try to find it with which/PATH
		
		return null;
	}
}
