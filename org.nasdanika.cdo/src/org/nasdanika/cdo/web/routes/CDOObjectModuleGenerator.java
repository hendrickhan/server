package org.nasdanika.cdo.web.routes;

public class CDOObjectModuleGenerator implements org.nasdanika.cdo.web.routes.Generator {


  protected static String nl;
  public static synchronized CDOObjectModuleGenerator create(String lineSeparator)
  {
    nl = lineSeparator;
    CDOObjectModuleGenerator result = new CDOObjectModuleGenerator();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "";
  protected final String TEXT_2 = NL + NL + "define([\"";
  protected final String TEXT_3 = "/session.js\", \"q\", \"jquery\", ";
  protected final String TEXT_4 = ", \"";
  protected final String TEXT_5 = ".js\"";
  protected final String TEXT_6 = "], function(session, Q, jquery) {" + NL + "\tconsole.log(\"Defining ";
  protected final String TEXT_7 = ".js\");" + NL + "    var data = {" + NL + "    \t";
  protected final String TEXT_8 = NL + "\t    \t";
  protected final String TEXT_9 = "," + NL + "\t    \t";
  protected final String TEXT_10 = NL + "    \t";
  protected final String TEXT_11 = NL + "    };  " + NL + "" + NL + "    session.";
  protected final String TEXT_12 = " = {" + NL + "" + NL + "        get delta() {" + NL + "        \t";
  protected final String TEXT_13 = NL + "        \t\t";
  protected final String TEXT_14 = NL + "        \t";
  protected final String TEXT_15 = NL + "       }," + NL + "       " + NL + "        set delta(delta) {" + NL + "        \t";
  protected final String TEXT_16 = NL + "        \t\t";
  protected final String TEXT_17 = NL + "        \t";
  protected final String TEXT_18 = NL + "        \t" + NL + "            dirty = false;" + NL + "        }," + NL + "" + NL + "        reset: function() {" + NL + "            delete data.someAttr.value;" + NL + "            dirty = false;" + NL + "        }   " + NL + "    };" + NL + "" + NL + "    var facade = {" + NL + "    " + NL + "    \t";
  protected final String TEXT_19 = NL + "\t    \t";
  protected final String TEXT_20 = "," + NL + "    \t";
  protected final String TEXT_21 = NL + NL + "\t\tget $session() {" + NL + "\t\t\treturn session; " + NL + "\t\t}, " + NL + "       " + NL + "        $store: function() {" + NL + "            // return a promise of this." + NL + "        }," + NL + "" + NL + "        $load: function() {" + NL + "            // return promise of this." + NL + "        }" + NL + "" + NL + "    };" + NL + "        " + NL + "\t";
  protected final String TEXT_22 = NL + "\t\t";
  protected final String TEXT_23 = NL + "\t";
  protected final String TEXT_24 = "    " + NL + "    " + NL + "    return facade;" + NL + "});";

public String generate(Object... args) throws Exception
  {
    final StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(TEXT_1);
    
	org.nasdanika.cdo.web.routes.CDOObjectJsExtensionRoute.ModuleGeneratorConfig config = (org.nasdanika.cdo.web.routes.CDOObjectJsExtensionRoute.ModuleGeneratorConfig) args[0];

    stringBuffer.append(TEXT_2);
    stringBuffer.append(config.getSessionPath());
    stringBuffer.append(TEXT_3);
     for (Object eager: config.getEager()) { 
    stringBuffer.append(TEXT_4);
    stringBuffer.append(config.getContext().getObjectPath(eager));
    stringBuffer.append(TEXT_5);
     } 
    stringBuffer.append(TEXT_6);
    stringBuffer.append(config.getObjectPath());
    stringBuffer.append(TEXT_7);
     
    		java.util.Iterator<String> dit = config.getDataDefinitions().iterator();
    		while (dit.hasNext()) {
    	
    stringBuffer.append(TEXT_8);
    stringBuffer.append(dit.next());
     if (dit.hasNext()) { 
    stringBuffer.append(TEXT_9);
     } 
    stringBuffer.append(TEXT_10);
     } 
    stringBuffer.append(TEXT_11);
    stringBuffer.append(config.getId());
    stringBuffer.append(TEXT_12);
     for (String getDeltaEntry: config.getGetDeltaEntries()) { 
    stringBuffer.append(TEXT_13);
    stringBuffer.append(getDeltaEntry);
    stringBuffer.append(TEXT_14);
     } 
    stringBuffer.append(TEXT_15);
     for (String setDeltaEntry: config.getSetDeltaEntries()) { 
    stringBuffer.append(TEXT_16);
    stringBuffer.append(setDeltaEntry);
    stringBuffer.append(TEXT_17);
     } 
    stringBuffer.append(TEXT_18);
     
    		java.util.Iterator<String> fit = config.getFacadeDefinitions().iterator();
    		while (fit.hasNext()) {
    	
    stringBuffer.append(TEXT_19);
    stringBuffer.append(fit.next());
    stringBuffer.append(TEXT_20);
     } 
    stringBuffer.append(TEXT_21);
     for (String preloadAction: config.getPreloadActions()) { 
    stringBuffer.append(TEXT_22);
    stringBuffer.append(preloadAction);
    stringBuffer.append(TEXT_23);
     } 
    stringBuffer.append(TEXT_24);
    return stringBuffer.toString();
  }
}