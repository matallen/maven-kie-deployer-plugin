package org.kie.deployer.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.kie.deployer.ProcessDeployer6Mojo;

public class Utils {

  public static String printPrivateVariables(String header, ProcessDeployer6Mojo x) {
	try{
	    StringBuffer sb=new StringBuffer(header).append("\n");
	    int longest=0;
	    Map<String, String> variables=new HashMap<String, String>();
	    for (Field f : x.getClass().getDeclaredFields()) {
	      String name=f.getName();
	      // Class<?> type=f.getType();
	      if (!f.isAccessible())
	        f.setAccessible(true);
	      if (name.length() > longest)
	        longest=name.length();
	      
	      String value=f.get(x)!=null?f.get(x).toString():"null";
	      
	      if (name.toLowerCase().contains("pass"))
	        value=String.format("%"+value.length()+"s","").replaceAll(" ","*");
	        
	      variables.put(name,value);
	    }
	    for (Map.Entry<String, String> e : variables.entrySet())
	      sb.append(String.format("%-" + longest + "s = %-1s",e.getKey(),e.getValue())).append("\n");
	    return sb.append("\n").toString();
	}catch(Exception sink){
		return "";
	}
  }

}
