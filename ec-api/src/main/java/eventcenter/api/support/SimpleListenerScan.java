package eventcenter.api.support;

import eventcenter.api.EventCenterConfig;
import eventcenter.api.EventListener;
import eventcenter.api.ListenerScan;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SimpleListenerScan implements ListenerScan {

	private List<String> scanPackages;
	
	private List<EventListener> listenersCache;
	
	final Logger logger = Logger.getLogger(this.getClass());
	
	public List<String> getScanPackages() {
		if(null == scanPackages)
			scanPackages = new ArrayList<String>();
		return scanPackages;
	}

	public void setScanPackages(List<String> scanPackages) {
		this.scanPackages = scanPackages;
		this.listenersCache = null;
	}

	@Override
	public EventCenterConfig getEventCenterConfig() {
		return null;
	}

	@Override
	public List<EventListener> getEventListener() {
		if(null != listenersCache)
			return listenersCache;
		
		listenersCache = new ArrayList<EventListener>();
		if(null == scanPackages || scanPackages.size() == 0)
			return listenersCache;
		
		/*for(String scanPackage : scanPackages){
			
		}*/
		return null;
	}
	
	/*private List<EventListener> scanPackage(String scanPacakge){
		try {
			List<String> pcks = getClassNamesFromPackage(scanPacakge);
			return null;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ArrayList<EventListener>();
		} 
	}*/
	
	public static List<String> getClassNamesFromPackage(String packageName) throws IOException, URISyntaxException{
	    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	    URL packageURL;
	    ArrayList<String> names = new ArrayList<String>();;

	    packageName = packageName.replace(".", "/");
	    packageURL = classLoader.getResource(packageName);

	    if(packageURL.getProtocol().equals("jar")){
	        String jarFileName;
	        JarFile jf ;
	        Enumeration<JarEntry> jarEntries;
	        String entryName;

	        // build jar file name, then loop through zipped entries
	        jarFileName = URLDecoder.decode(packageURL.getFile(), "UTF-8");
	        jarFileName = jarFileName.substring(5,jarFileName.indexOf("!"));
	        jf = new JarFile(jarFileName);
	        jarEntries = jf.entries();
	        while(jarEntries.hasMoreElements()){
	            entryName = jarEntries.nextElement().getName();
	            if(entryName.startsWith(packageName) && entryName.length()>packageName.length()+5){
	                entryName = entryName.substring(packageName.length(),entryName.lastIndexOf('.'));
	                names.add(entryName);
	            }
	        }

	    // loop through files in classpath
	    }else{
	    	URI uri = new URI(packageURL.toString());
	    	File folder = new File(uri.getPath());
	        // won't work with path which contains blank (%20)
	        // File folder = new File(packageURL.getFile()); 
	        File[] contenuti = folder.listFiles();
	        String entryName;
	        for(File actual: contenuti){
	            entryName = actual.getName();
	            if(!isClassFile(entryName)){
	            	// TODO 处理文件夹类型
	            	continue;
	            }
	            entryName = entryName.substring(0, entryName.lastIndexOf('.'));
	            names.add(entryName);
	        }
	    }
	    return names;
	}
	
	private static boolean isClassFile(String entryName){
		return entryName.endsWith(".class");
	}

}
