package org.nasdanika.workspace.wizard.render.app;

public class SetHeightScriptRenderer {


  protected static String nl;
  public static synchronized SetHeightScriptRenderer create(String lineSeparator)
  {
    nl = lineSeparator;
    SetHeightScriptRenderer result = new SetHeightScriptRenderer();
    nl = null;
    return result;
  }

  public final String NL = nl == null ? (System.getProperties().getProperty("line.separator")) : nl;
  protected final String TEXT_1 = "require(['jquery', '";
  protected final String TEXT_2 = "/";
  protected final String TEXT_3 = "/resources/js/left-panel.js'], function(jQuery, tocTreePromise) { " + NL + "\t" + NL + "\t// Set padding to 0 in panel body." + NL + "\tjQuery(\"#docAppPanel > div.panel-body\").css(\"padding\", \"0px\");" + NL + "\t" + NL + "\tvar body = jQuery(\"body\");" + NL + "\tvar docContent = jQuery('#doc-content');\t" + NL + "\tvar toc = jQuery('#toc');" + NL + "\tvar oldBottom = -1;" + NL + "\t" + NL + "\tfunction resizeHandler() {\t\t" + NL + "\t\tvar delta = window.innerHeight - body.height();" + NL + "\t\tdelta-=5; // offset - app panel margin" + NL + "\t\tif (delta!=0) {" + NL + "\t\t\tdocContent.height(docContent.height()+delta);" + NL + "\t\t\tdocContent.css(\"overflow-y\", \"scroll\");" + NL + "\t\t}" + NL + "\t\t" + NL + "\t\tvar docContentBottom = docContent[0].getBoundingClientRect().bottom;" + NL + "\t\tif (docContentBottom!=oldBottom) {" + NL + "\t\t\toldBottom = docContentBottom;" + NL + "\t\t\tvar tocRect = toc[0].getBoundingClientRect();" + NL + "\t\t\tvar tocHeight = docContentBottom - tocRect.top - 1;" + NL + "\t\t\ttoc.height(tocHeight);" + NL + "\t\t\t" + NL + "\t\t\t// Scrolling current selection into view" + NL + "\t\t\ttocTreePromise.then(function(tocTree) {" + NL + "\t\t\t\tvar selected = tocTree.jstree(\"get_top_selected\");" + NL + "\t\t\t\tif (selected.length>0) {" + NL + "\t\t\t\t\tvar nodeElement = jQuery('#'+selected[0]+\"_anchor\");" + NL + "\t\t\t\t\tvar nodeOffsetTop = nodeElement[0].offsetTop;" + NL + "\t\t\t\t\tvar tocScrollTop = toc.scrollTop() + toc[0].offsetTop;" + NL + "\t\t\t\t\tvar nodeOffsetBottom = nodeOffsetTop + nodeElement.height();" + NL + "\t\t\t\t\tvar tocScrollBottom = tocScrollTop+toc.height();" + NL + "\t\t\t\t\tif (nodeOffsetTop < tocScrollTop || nodeOffsetBottom > tocScrollBottom) {\t\t\t" + NL + "\t\t\t\t\t\tvar targetScrollTop = nodeOffsetTop - toc.height()/2;" + NL + "\t\t\t\t\t\ttoc.animate({ scrollTop: targetScrollTop+\"px\" });" + NL + "\t\t\t\t\t}\t\t\t\t\t\t\t" + NL + "\t\t\t\t\t" + NL + "\t\t\t\t}" + NL + "\t\t\t})" + NL + "\t\t}" + NL + "\t\t" + NL + "\t}" + NL + "\t" + NL + "\tjQuery(window).resize(resizeHandler);" + NL + "\t" + NL + "\tsetInterval(resizeHandler, 100);" + NL + "});";

public String generate(org.nasdanika.workspace.wizard.WorkspaceWizard wizard) throws Exception
  {
    final StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(TEXT_1);
    stringBuffer.append(wizard.getContextPath());
    stringBuffer.append(wizard.getRoutingServletAlias());
    stringBuffer.append(TEXT_2);
    stringBuffer.append(wizard.getDocRoutePath());
    stringBuffer.append(TEXT_3);
    return stringBuffer.toString();
  }
}