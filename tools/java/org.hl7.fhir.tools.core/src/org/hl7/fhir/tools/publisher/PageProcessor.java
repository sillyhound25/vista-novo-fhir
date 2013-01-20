package org.hl7.fhir.tools.publisher;
/*
Copyright (c) 2011-2012, HL7, Inc
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this 
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
   this list of conditions and the following disclaimer in the documentation 
   and/or other materials provided with the distribution.
 * Neither the name of HL7 nor the names of its contributors may be used to 
   endorse or promote products derived from this software without specific 
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

*/
import java.io.File;
import java.io.FileOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.definitions.Config;
import org.hl7.fhir.definitions.generators.specification.DictHTMLGenerator;
import org.hl7.fhir.definitions.generators.specification.TerminologyNotesGenerator;
import org.hl7.fhir.definitions.generators.specification.XmlSpecGenerator;
import org.hl7.fhir.definitions.model.BindingSpecification;
import org.hl7.fhir.definitions.model.BindingSpecification.Binding;
import org.hl7.fhir.definitions.model.BindingSpecification.BindingExtensibility;
import org.hl7.fhir.definitions.model.BindingSpecification.BindingStrength;
import org.hl7.fhir.definitions.model.DefinedCode;
import org.hl7.fhir.definitions.model.Definitions;
import org.hl7.fhir.definitions.model.ElementDefn;
import org.hl7.fhir.definitions.model.EventDefn;
import org.hl7.fhir.definitions.model.EventUsage;
import org.hl7.fhir.definitions.model.Example;
import org.hl7.fhir.definitions.model.Invariant;
import org.hl7.fhir.definitions.model.ProfileDefn;
import org.hl7.fhir.definitions.model.RegisteredProfile;
import org.hl7.fhir.definitions.model.ResourceDefn;
import org.hl7.fhir.definitions.model.SearchParameter;
import org.hl7.fhir.definitions.model.SearchParameter.SearchType;
import org.hl7.fhir.definitions.model.TypeRef;
import org.hl7.fhir.definitions.parsers.TypeParser;
import org.hl7.fhir.utilities.CSFile;
import org.hl7.fhir.utilities.IniFile;
import org.hl7.fhir.utilities.Logger;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;

public class PageProcessor implements Logger  {

  private static final String SIDEBAR_SPACER = "<p>&nbsp;</p>\r\n";
  private Definitions definitions;
  private FolderManager folders;
  private String version;
  private Navigation navigation;
  private List<PlatformGenerator> referenceImplementations = new ArrayList<PlatformGenerator>();
  private IniFile ini;
  private Calendar genDate = Calendar.getInstance();
  private Date start = new Date();
  private String prevSidebar;
  private List<String> orderedResources = new ArrayList<String>();
  private Map<String, SectionTracker> sectionTrackerCache = new HashMap<String, SectionTracker>(); 
  private Map<String, TocEntry> toc = new HashMap<String, TocEntry>();
  
//  private boolean notime;
  
  private String dictForDt(String dt) throws Exception {
	  File tmp = File.createTempFile("tmp", ".tmp");
	  DictHTMLGenerator gen = new DictHTMLGenerator(new FileOutputStream(tmp));
	  TypeParser tp = new TypeParser();
	  TypeRef t = tp.parse(dt).get(0);
	  ElementDefn e = definitions.getElementDefn(t.getName());
	  if (e == null) {
		  gen.close();
		  throw new Exception("unable to find definition for "+ dt);
	  } 
	  else {
		  gen.generate(e);
		  gen.close();
	  }
	  String val = TextFile.fileToString(tmp.getAbsolutePath())+"\r\n";
	  tmp.delete();
	  return val; 
  }

  private String tsForDt(String dt) throws Exception {
	  File tmp = File.createTempFile("tmp", ".tmp");
	  tmp.deleteOnExit();
	  TerminologyNotesGenerator gen = new TerminologyNotesGenerator(new FileOutputStream(tmp), this);
	  TypeParser tp = new TypeParser();
	  TypeRef t = tp.parse(dt).get(0);
	  ElementDefn e = definitions.getElementDefn(t.getName());
	  if (e == null) {
		  gen.close();
		  throw new Exception("unable to find definition for "+ dt);
	  } 
	  else {
		  gen.generate(e, definitions.getBindings());
		  gen.close();
	  }
	  String val = TextFile.fileToString(tmp.getAbsolutePath())+"\r\n";
	  tmp.delete();
	  return val;
  }
  
  private String xmlForDt(String dt, String pn) throws Exception {
	  File tmp = File.createTempFile("tmp", ".tmp");
	  tmp.deleteOnExit();
	  XmlSpecGenerator gen = new XmlSpecGenerator(new FileOutputStream(tmp), pn == null ? null : pn.substring(0, pn.indexOf("."))+"-definitions.htm", null, definitions);
	  TypeParser tp = new TypeParser();
	  TypeRef t = tp.parse(dt).get(0);
	  ElementDefn e = definitions.getElementDefn(t.getName());
	  if (e == null) {
		  gen.close();
		  throw new Exception("unable to find definition for "+ dt);
	  } 
	  else {
		  gen.generate(e);
		  gen.close();
	  }
	  String val = TextFile.fileToString(tmp.getAbsolutePath())+"\r\n";
	  tmp.delete();
	  return val; 
  }

  private String generateSideBar() throws Exception {
    if (prevSidebar != null)
      return prevSidebar;
    List<String> links = new ArrayList<String>();
    
    StringBuilder s = new StringBuilder();
    s.append("<div class=\"sidebar\">\r\n");
    s.append("<p><a href=\"http://hl7.org/fhir\" title=\"Fast Healthcare Interoperability Resources - Home Page\"><img border=\"0\" src=\"flame16.png\" style=\"vertical-align: text-bottom\"/></a> <a href=\"http://hl7.org/fhir\" title=\"Fast Healthcare Interoperability Resources - Home Page\"><b>FHIR</b></a> &copy; <a href=\"http://hl7.org\">HL7.org</a></p>\r\n");
    s.append("<p class=\"note\">Version v"+getVersion()+" - Under Development</p>\r\n"); 

    for (Navigation.Category c : navigation.getCategories()) {
      if (!"nosidebar".equals(c.getMode())) {
        if (c.getLink() != null) {
          s.append("  <h2><a href=\""+c.getLink()+".htm\">"+c.getName()+"</a></h2>\r\n");
          links.add(c.getLink());
        }
        else
          s.append("  <h2>"+c.getName()+"</h2>\r\n");
        s.append("  <ul>\r\n");
        for (Navigation.Entry e : c.getEntries()) {
          if (e.getLink() != null) {
            links.add(e.getLink());
            s.append("    <li><a href=\""+e.getLink()+".htm\">"+Utilities.escapeXml(e.getName())+"</a></li>\r\n");
          } else
            s.append("    <li>"+e.getName()+"</li>\r\n");
        }
        if (c.getEntries().size() ==0 && c.getLink().equals("resources")) {
          List<String> list = new ArrayList<String>();
          list.addAll(definitions.getResources().keySet());
          Collections.sort(list);

          for (String rn : list) {
            if (!links.contains(rn.toLowerCase())) {
              ResourceDefn r = definitions.getResourceByName(rn);
              orderedResources.add(r.getName());
              s.append("    <li><a href=\""+rn.toLowerCase()+".htm\">"+Utilities.escapeXml(r.getName())+"</a></li>\r\n");
            }
          }

        }
        s.append("  </ul>\r\n");
      }
    }
    s.append(SIDEBAR_SPACER);
    s.append("<p><a href=\"http://hl7.org\"><img border=\"0\" src=\"hl7logo.png\"/></a></p>\r\n");

    s.append("</div>\r\n");
    prevSidebar = s.toString();
    return prevSidebar;
  }

  private String combineNotes(List<String> followUps, String notes) {
    String s = "";
    if (notes != null && !notes.equals(""))
      s = notes;
    if (followUps.size() > 0)
      if (s != "")
        s = s + "<br/>Follow ups: "+Utilities.asCSV(followUps);
      else
        s = "Follow ups: "+Utilities.asCSV(followUps);
    return s;      
  }

  private String describeMsg(List<String> resources, List<String> aggregations) {
    if (resources.size() == 0 && aggregations.size() == 0)
      return "<font color=\"silver\">--</font>";
    else {
      String s = resources.size() == 0 ? "" : Utilities.asCSV(resources);
      
      if (aggregations.size() == 0)
        return s;
      else
        return s + "<br/>"+Utilities.asHtmlBr("&nbsp;"+resources.get(0), aggregations)+"";
    }      
  }


  public String processPageIncludes(String file, String src) throws Exception {
    while (src.contains("<%") || src.contains("[%"))
    {
      int i1 = src.indexOf("<%");
      int i2 = src.indexOf("%>");
      if (i1 == -1) {
        i1 = src.indexOf("[%");
        i2 = src.indexOf("%]");
      }
      String s1 = src.substring(0, i1);
      String s2 = src.substring(i1 + 2, i2).trim();
      String s3 = src.substring(i2+2);
      String name = file.substring(0,file.indexOf(".")); 

      String[] com = s2.split(" ");
      if (com.length == 2 && com[0].equals("dt")) 
        src = s1+xmlForDt(com[1], file)+tsForDt(com[1])+s3;
      else if (com.length == 2 && com[0].equals("dt.constraints")) 
        src = s1+genConstraints(com[1])+s3;
      else if (com.length == 2 && com[0].equals("dt.restrictions")) 
        src = s1+genRestrictions(com[1])+s3;
      else if (com.length == 2 && com[0].equals("dictionary"))
        src = s1+dictForDt(com[1])+s3;
      else if (com[0].equals("dtheader"))
        src = s1+dtHeader(name, com.length > 1 ? com[1] : null)+s3;
      else if (com[0].equals("xmlheader"))
        src = s1+xmlHeader(name, com.length > 1 ? com[1] : null)+s3;
      else if (com[0].equals("txheader"))
        src = s1+txHeader(name, com.length > 1 ? com[1] : null)+s3;
      else if (com[0].equals("extheader"))
        src = s1+extHeader(name, com.length > 1 ? com[1] : null)+s3;
      else if (com[0].equals("atomheader"))
        src = s1+atomHeader(name, com.length > 1 ? com[1] : null)+s3;
      else if (com[0].equals("codelist"))
        src = s1+codelist(name, com.length > 1 ? com[1] : null)+s3;
      else if (com[0].equals("resheader"))
        src = s1+resHeader("document", "Document", com.length > 1 ? com[1] : null)+s3;
      else if (com[0].equals("onthispage"))
        src = s1+onThisPage(s2.substring(com[0].length()+1))+s3;
      else if (com.length != 1)
        throw new Exception("Instruction <%"+s2+"%> not understood parsing page "+file);
      else if (com[0].equals("pageheader"))
        src = s1+pageHeader(name.toUpperCase().substring(0, 1)+name.substring(1))+s3;
      else if (com[0].equals("footer"))
        src = s1+TextFile.fileToString(folders.srcDir + "footer.htm")+s3;
      else if (com[0].equals("sidebar"))
        src = s1+generateSideBar()+s3;
      else if (com[0].equals("title"))
        src = s1+name.toUpperCase().substring(0, 1)+name.substring(1)+s3;
      else if (com[0].equals("name"))
        src = s1+name+s3;
      else if (com[0].equals("version"))
        src = s1+version+s3;
      else if (com[0].equals("gendate"))
        src = s1+Config.DATE_FORMAT().format(new Date())+s3;
      else if (com[0].equals("maindiv"))
        src = s1+"<div class=\"content\">"+s3;
      else if (com[0].equals("/maindiv"))
        src = s1+"</div>"+s3;
      else if (com[0].equals("events"))
        src = s1 + getEventsTable()+ s3;
      else if (com[0].equals("resourcecodes"))
        src = s1 + genResCodes() + s3;
      else if (com[0].equals("datatypecodes"))
        src = s1 + genDTCodes() + s3;
      else if (com[0].equals("bindingtable-codelists"))
        src = s1 + genBindingTable(true) + s3;
      else if (com[0].equals("bindingtable"))
        src = s1 + genBindingsTable() + s3;
      else if (com[0].equals("codeslist"))
        src = s1 + genCodeSystemsTable() + s3;
      else if (com[0].equals("bindingtable-others"))
        src = s1 + genBindingTable(false) + s3;
      else if (com[0].equals("resimplall"))
        src = s1 + genResImplList() + s3;
      else if (com[0].equals("impllist"))
        src = s1 + genReferenceImplList() + s3;
      else if (com[0].equals("txurl"))
        src = s1 + "http://hl7.org/fhir/"+Utilities.fileTitle(file) + s3;
      else if (com[0].equals("toc"))
        src = s1 + generateToc() + s3;
      else if (com[0].equals("txdef"))
        src = s1 + generateCodeDefinition(Utilities.fileTitle(file)) + s3;
      else if (com[0].equals("txusage"))
        src = s1 + generateBSUsage(Utilities.fileTitle(file)) + s3;
      else if (com[0].equals("txsummary"))
        src = s1 + generateCodeTable(Utilities.fileTitle(file)) + s3;
      else 
        throw new Exception("Instruction <%"+s2+"%> not understood parsing page "+file);
    }
    return src;
  }

  private String onThisPage(String tail) {
    String[] entries = tail.split("\\|");
    StringBuilder b = new StringBuilder();
    b.append("<div class=\"itoc\">\r\n<p>On This Page:</p>\r\n");
    for (String e : entries) {
      String[] p = e.split("#");
      if (p.length == 2)
        b.append("<p class=\"link\"><a href=\"#"+p[1]+"\">"+Utilities.escapeXml(p[0])+"</a></p>");
      if (p.length == 1)
        b.append("<p class=\"link\"><a href=\"#\">"+Utilities.escapeXml(p[0])+"</a></p>");
    }
    b.append("</div>\r\n");
    return b.toString();
  }

  private class TocSort implements Comparator<String> {

    public int compare(String arg0, String arg1) {
      String[] a0 = arg0.split("\\.");
      String[] a1 = arg1.split("\\.");
      for (int i = 0; i < Math.min(a0.length, a1.length); i++) {
        int i0 = Integer.parseInt(a0[i]);
        int i1 = Integer.parseInt(a1[i]);
        if (i0 != i1)
          return i0-i1;
      }
      return (a0.length - a1.length);
    }
  }
  
  private String generateToc() {
    List<String> entries = new ArrayList<String>();
    entries.addAll(toc.keySet());
    
    Collections.sort(entries, new TocSort());
    
    StringBuilder b = new StringBuilder();
    for (String s : entries) {
      int i = 0;
      for (char c : s.toCharArray()) {
        if (c == '.')
          i++;
      }
      TocEntry t = toc.get(s); 
      if (i < 3) {
        for (int j = 0; j < i; j++)
          b.append("&nbsp;&nbsp;");
        b.append("<a href=\""+t.getLink()+"#"+t.getValue()+"\">"+t.getValue()+"</a> "+Utilities.escapeXml(t.getText())+"<br/>\r\n");
      }
    }

    return "<p>"+b.toString()+"</p>\r\n";
  }

  private String generateBSUsage(String name) {
    BindingSpecification cd = definitions.getBindingByReference("#"+name);
    StringBuilder b = new StringBuilder();
    for (ResourceDefn r : definitions.getResources().values())
      scanForUsage(b, cd, r.getRoot(), r.getName().toLowerCase()+".htm#def");
    for (ElementDefn e : definitions.getInfrastructure().values())
      scanForUsage(b, cd, e, "xml.htm#"+e.getName());
    for (ElementDefn e : definitions.getTypes().values())
      scanForUsage(b, cd, e, "datatypes.htm#"+e.getName());
    for (ElementDefn e : definitions.getStructures().values())
      if (!e.getName().equals("DocumentInformation"))
        scanForUsage(b, cd, e, "datatypes.htm#"+e.getName());
      
    if (b.length() == 0)
      return "<p>\r\nThese codes are not currently used\r\n</p>\r\n";
    else
      return "<p>\r\nThese codes are used in the follow places:\r\n</p>\r\n<ul>\r\n"+b.toString()+"</ul>\r\n";
  }

  private void scanForUsage(StringBuilder b, BindingSpecification cd, ElementDefn e, String ref) {
    scanForUsage(b, cd, e, "", ref);
    
  }

  private void scanForUsage(StringBuilder b, BindingSpecification cd, ElementDefn e, String path, String ref) {
    path = path.equals("") ? e.getName() : path+"."+e.getName();
    if (e.hasBinding() && e.getBindingName().equals(cd.getName())) {
      b.append(" <li><a href=\""+ref+"\">"+path+"</a></li>\r\n");
    }
    for (ElementDefn c : e.getElements()) {
      scanForUsage(b, cd, c, path, ref);
    }
  }

  private String generateCodeDefinition(String name) {
    BindingSpecification cd = definitions.getBindingByReference("#"+name);
    return Utilities.escapeXml(cd.getDefinition());
  }

  private String generateCodeTable(String name) {
    BindingSpecification cd = definitions.getBindingByReference("#"+name);
    StringBuilder s = new StringBuilder();
    s.append("    <table class=\"codes\">\r\n");
    boolean hasComment = false;
    boolean hasDefinition = false;
    for (DefinedCode c : cd.getCodes()) {
      hasComment = hasComment || c.hasComment();
      hasDefinition = hasDefinition || c.hasDefinition();
    }
    for (DefinedCode c : cd.getCodes()) {
      if (hasComment)
        s.append("    <tr><td>"+Utilities.escapeXml(c.getCode())+"</td><td>"+Utilities.escapeXml(c.getDefinition())+"</td><td>"+Utilities.escapeXml(c.getComment())+"</td></tr>");
      else if (hasDefinition)
        s.append("    <tr><td>"+Utilities.escapeXml(c.getCode())+"</td><td colspan=\"2\">"+Utilities.escapeXml(c.getDefinition())+"</td></tr>");
      else
        s.append("    <tr><td colspan=\"3\">"+Utilities.escapeXml(c.getCode())+"</td></tr>");
    }
    s.append("    </table>\r\n");
    return s.toString();
  }

  private String genProfileConstraints(ResourceDefn res) throws Exception {
    ElementDefn e = res.getRoot();
    StringBuilder b = new StringBuilder();
    generateConstraints("", e, b);
    if (b.length() > 0)
      return "<p>Constraints</p><ul>"+b+"</ul>";
    else
      return "";
  }
  
  private String genResourceConstraints(ResourceDefn res) throws Exception {
    ElementDefn e = res.getRoot();
    StringBuilder b = new StringBuilder();
    generateConstraints("", e, b);
    if (b.length() > 0)
      return "<p>Constraints</p><ul>"+b+"</ul>";
    else
      return "";
  }
  
  private String genRestrictions(String name) throws Exception {
    StringBuilder b = new StringBuilder();
    StringBuilder b2 = new StringBuilder();
    for (DefinedCode c : definitions.getConstraints().values()) {
      if (c.getComment().equals(name)) {
        b.append("<a name=\""+c.getCode()+"\"></a>\r\n");
        b2.append(" <tr><td>"+c.getCode()+"</td><td>"+Utilities.escapeXml(c.getDefinition())+"</td></tr>\r\n");
      }
    }
    if (b.length() > 0) 
      return b.toString()+"<table class=\"list\">\r\n"+b2.toString()+"</table>\r\n";
    else
      return "";
  }

  private String genConstraints(String name) throws Exception {
    ElementDefn e = definitions.getElementDefn(name);
    StringBuilder b = new StringBuilder();
    generateConstraints("", e, b);
    if (b.length() > 0)
      return "<ul>"+b+"</ul>";
    else
      return "";
  }

  private void generateConstraints(String path, ElementDefn e, StringBuilder b) {
    for (String n : e.getInvariants().keySet()) {
      Invariant inv = e.getInvariants().get(n);
      if ("".equals(path))
        b.append("<li>"+Utilities.escapeXml(inv.getEnglish())+" (xpath: "+Utilities.escapeXml(inv.getXpath())+")</li>");
      else
        b.append("<li>"+Utilities.escapeXml(inv.getEnglish())+" (xpath on "+path+": "+Utilities.escapeXml(inv.getXpath())+")</li>");
    }
    for (ElementDefn c : e.getElements()) {
      generateConstraints(path+"/"+c.getName(), c, b);
    }    
  }

  private String pageHeader(String n) {
    return "<div class=\"navtop\"><ul class=\"navtop\"><li class=\"spacerright\" style=\"width: 500px\"><span>&nbsp;</span></li><li class=\"wiki\"><span><a href=\"http://wiki.hl7.org/index.php?title=FHIR_"+n+"_Page\">Community Input (wiki)</a></span></li></ul></div>\r\n";
  }
  
  private String dtHeader(String n, String mode) {
    if (n.contains("-"))
      n = n.substring(0, n.indexOf('-'));
    StringBuilder b = new StringBuilder();
    b.append("<div class=\"navtop\">");
    b.append("<ul class=\"navtop\"><li class=\"spacerleft\"><span>&nbsp;</span></li>");
    if (mode == null || mode.equals("content"))
      b.append("<li class=\"selected\"><span>Content</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+".htm\">Content</a></span></li>");
    if ("examples".equals(mode))
      b.append("<li class=\"selected\"><span>Examples</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+"-examples.htm\">Examples</a></span></li>");
    if ("definitions".equals(mode))
      b.append("<li class=\"selected\"><span>Formal Definitions</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+"-definitions.htm\">Formal Definitions</a></span></li>");
    b.append("<li class=\"spacerright\" style=\"width: 270px\"><span>&nbsp;</span></li>");
    b.append("<li class=\"wiki\"><span><a href=\"http://wiki.hl7.org/index.php?title=FHIR_"+n.toUpperCase().substring(0, 1)+n.substring(1)+"_Page\">Community Input (wiki)</a></span></li>");
    b.append("</ul></div>\r\n");
    return b.toString();
  }

  private String xmlHeader(String n, String mode) {
    if (n.contains("-"))
      n = n.substring(0, n.indexOf('-'));
    StringBuilder b = new StringBuilder();
    b.append("<div class=\"navtop\">");
    b.append("<ul class=\"navtop\"><li class=\"spacerleft\"><span>&nbsp;</span></li>");
    if (mode == null || mode.equals("content"))
      b.append("<li class=\"selected\"><span>Content</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+".htm\">Content</a></span></li>");
    if ("examples".equals(mode))
      b.append("<li class=\"selected\"><span>Examples</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+"-examples.htm\">Examples</a></span></li>");
    if ("definitions".equals(mode))
      b.append("<li class=\"selected\"><span>Formal Definitions</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+"-definitions.htm\">Formal Definitions</a></span></li>");
    b.append("<li class=\"spacerright\" style=\"width: 270px\"><span>&nbsp;</span></li>");
    b.append("<li class=\"wiki\"><span><a href=\"http://wiki.hl7.org/index.php?title=FHIR_"+n.toUpperCase().substring(0, 1)+n.substring(1)+"_Page\">Community Input (wiki)</a></span></li>");
    b.append("</ul></div>\r\n");
    return b.toString();
  }

  private String extHeader(String n, String mode) {
    if (n.contains("-"))
      n = n.substring(0, n.indexOf('-'));
    StringBuilder b = new StringBuilder();
    b.append("<div class=\"navtop\">");
    b.append("<ul class=\"navtop\"><li class=\"spacerleft\"><span>&nbsp;</span></li>");
    if (mode == null || mode.equals("content"))
      b.append("<li class=\"selected\"><span>Content</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+".htm\">Content</a></span></li>");
    if ("examples".equals(mode))
      b.append("<li class=\"selected\"><span>Examples</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+"-examples.htm\">Examples</a></span></li>");
    if ("definitions".equals(mode))
      b.append("<li class=\"selected\"><span>Formal Definitions</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+"-definitions.htm\">Formal Definitions</a></span></li>");
    b.append("<li class=\"spacerright\" style=\"width: 270px\"><span>&nbsp;</span></li>");
    b.append("<li class=\"wiki\"><span><a href=\"http://wiki.hl7.org/index.php?title=FHIR_"+n.toUpperCase().substring(0, 1)+n.substring(1)+"_Page\">Community Input (wiki)</a></span></li>");
    b.append("</ul></div>\r\n");
    return b.toString();
  }

  private String txHeader(String n, String mode) {
    if (n.contains("-"))
      n = n.substring(0, n.indexOf('-'));
    StringBuilder b = new StringBuilder();
    b.append("<div class=\"navtop\">");
    b.append("<ul class=\"navtop\"><li class=\"spacerleft\"><span>&nbsp;</span></li>");
    if (mode == null || mode.equals("content"))
      b.append("<li class=\"selected\"><span>Content</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+".htm\">Content</a></span></li>");
    if ("codes".equals(mode))
      b.append("<li class=\"selected\"><span>Defined Codes</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+"-codes.htm\">Defined Codes</a></span></li>");
    if ("bindings".equals(mode))
      b.append("<li class=\"selected\"><span>Bindings</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+"-bindings.htm\">Bindings</a></span></li>");
    b.append("<li class=\"spacerright\" style=\"width: 370px\"><span>&nbsp;</span></li>");
    b.append("<li class=\"wiki\"><span><a href=\"http://wiki.hl7.org/index.php?title=FHIR_"+n.toUpperCase().substring(0, 1)+n.substring(1)+"_Page\">Community Input (wiki)</a></span></li>");
    b.append("</ul></div>\r\n");
    return b.toString();
  }

  private String atomHeader(String n, String mode) {
    if (n.contains("-"))
      n = n.substring(0, n.indexOf('-'));
    StringBuilder b = new StringBuilder();
    b.append("<div class=\"navtop\">");
    b.append("<ul class=\"navtop\"><li class=\"spacerleft\"><span>&nbsp;</span></li>");
    if (mode == null || mode.equals("content"))
      b.append("<li class=\"selected\"><span>Content</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+".htm\">Content</a></span></li>");
    if ("examples".equals(mode))
      b.append("<li class=\"selected\"><span>Examples</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+"-examples.htm\">Examples</a></span></li>");
    b.append("<li class=\"spacerright\" style=\"width: 370px\"><span>&nbsp;</span></li>");
    b.append("<li class=\"wiki\"><span><a href=\"http://wiki.hl7.org/index.php?title=FHIR_"+n.toUpperCase().substring(0, 1)+n.substring(1)+"_Page\">Community Input (wiki)</a></span></li>");
    b.append("</ul></div>\r\n");
    return b.toString();
  }

  private String codelist(String n, String mode) throws Exception {
    BindingSpecification bs = definitions.getBindingByName(mode);
    if (bs == null)
      throw new Exception("Unable to find code list '"+mode+"'");
    if (bs.getCodes().size() == 0)
      throw new Exception("Code list '"+mode+"' is empty/not defined");
    boolean hasComments = false;
    for (DefinedCode c : bs.getCodes())
      hasComments = hasComments || c.hasComment();
    
    StringBuilder b = new StringBuilder();
    b.append("<h3>"+bs.getDescription()+"</h3>\r\n");
    b.append("<table class=\"codes\">\r\n");
    for (DefinedCode c : bs.getCodes()) {
      if (hasComments)
        b.append(" <tr><td>"+c.getCode()+"</td><td>"+Utilities.escapeXml(c.getDefinition())+"</td><td>"+Utilities.escapeXml(c.getComment())+"</td></tr>\r\n");
      else
        b.append(" <tr><td>"+c.getCode()+"</td><td>"+Utilities.escapeXml(c.getDefinition())+"</td></tr>\r\n");
    }
    b.append("</table>\r\n");
    return b.toString();
  }

  private String resHeader(String n, String title, String mode) {
    if (n.contains("-"))
      n = n.substring(0, n.indexOf('-'));
    StringBuilder b = new StringBuilder();
    b.append("<div class=\"navtop\">");
    b.append("<ul class=\"navtop\">");
    b.append("<li class=\"spacerleft\"><span>&nbsp;</span></li>");
    if (mode == null || mode.equals("content"))
      b.append("<li class=\"selected\"><span>Content</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+".htm\">Content</a></span></li>");
    String pages = ini.getStringProperty("resource-pages", title.toLowerCase());
    if (!Utilities.noString(pages)) {
      for (String p : pages.split(",")) {
        String t = ini.getStringProperty("page-titles", p);
        if (t.equals(mode)) 
          b.append("<li class=\"selected\"><span>"+t+"</span></li>");
        else
          b.append("<li class=\"nselected\"><span><a href=\""+p+"\">"+t+"</a></span></li>");
      }
    }
    if ("examples".equals(mode))
      b.append("<li class=\"selected\"><span>Examples</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+"-examples.htm\">Examples</a></span></li>");
    if ("definitions".equals(mode))
      b.append("<li class=\"selected\"><span>Formal Definitions</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+"-definitions.htm\">Formal Definitions</a></span></li>");

    if ("explanations".equals(mode))
      b.append("<li class=\"selected\"><span>Design Notes</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+"-explanations.htm\">Design Notes</a></span></li>");
    
    if ("profiles".equals(mode))
      b.append("<li class=\"selected\"><span>Profiles</span></li>");
    else
      b.append("<li class=\"nselected\"><span><a href=\""+n+"-profiles.htm\">Profiles</a></span></li>");
    
    b.append("<li class=\"spacerright\"><span>&nbsp;</span></li>");
    b.append("<li class=\"wiki\"><span><a href=\"http://wiki.hl7.org/index.php?title=FHIR_"+n+"_Page\">Community Input (wiki)</a></span></li>");
    b.append("</ul></div>\r\n");
    return b.toString();
  }

  private String genCodeSystemsTable() throws Exception {
    StringBuilder s = new StringBuilder();
    s.append("<table class=\"codes\">\r\n");
    List<String> names = new ArrayList<String>();
    for (String n : definitions.getBindings().keySet()) {
      if (definitions.getBindingByName(n).getBinding() == Binding.CodeList)
       names.add(n);
    }
    Collections.sort(names);
    for (String n : names) {
      BindingSpecification cd = definitions.getBindingByName(n);
      s.append(" <tr><td><a href=\""+cd.getReference().substring(1)+".htm\">http://hl7.org/fhir/"+cd.getReference().substring(1)+"</a></td><td>"+Utilities.escapeXml(cd.getDefinition())+"</td></tr>\r\n");
    }
    s.append("</table>\r\n");
    return s.toString();
  }


  private String genBindingsTable() {
    StringBuilder s = new StringBuilder();
    s.append("<table class=\"codes\">\r\n");
    s.append(" <tr><th>Name</th><th>Definition</th><th>Strength</th><th>Reference</th></tr>\r\n");
    List<String> names = new ArrayList<String>();
    for (String n : definitions.getBindings().keySet()) {
      names.add(n);
    }
    Collections.sort(names);
    for (String n : names) {
      if (!n.startsWith("*")) {
        BindingSpecification cd = definitions.getBindingByName(n);
        s.append(" <tr><td>"+Utilities.escapeXml(cd.getName())+"</td><td>"+Utilities.escapeXml(cd.getDefinition())+"</td><td>"+Utilities.escapeXml(cd.getBindingStrength().toString()));
        if (cd.getExtensibility() != BindingExtensibility.Complete)
          s.append(" (Extensible)");
        if (cd.getBinding() == Binding.Special) {
          
          if (cd.getName().equals("MessageEvent"))
            s.append("</td><td><a href=\"message.htm#Events\">Message Event List</a></td></tr>\r\n");
          else if (cd.getName().equals("ResourceType"))
            s.append("</td><td><a href=\"terminologies.htm#ResourceType\">Resource Type names</a></td></tr>\r\n");
          else if (cd.getName().equals("FHIRContentType"))
            s.append("</td><td><a href=\"terminologies.htm#fhircontenttypes\">Resource or Data Type name</a></td></tr>\r\n");
          else 
            s.append("</td><td>???</td></tr>\r\n");
                    
        } else if (cd.getBinding() == Binding.CodeList)
          s.append("</td><td><a href=\""+cd.getReference().substring(1)+".htm\">http://hl7.org/fhir/"+cd.getReference().substring(1)+"</a></td></tr>\r\n");          
        else if (cd.hasReference())
          s.append("</td><td><a href=\""+cd.getReference()+"\">"+Utilities.escapeXml(cd.getDescription())+"</a></td></tr>\r\n");
        else if (Utilities.noString(cd.getDescription()))
          s.append("</td><td style=\"color: grey\">"+Utilities.escapeXml(cd.getBinding().toString())+"</td></tr>\r\n");
        else
          s.append("</td><td>"+Utilities.escapeXml(cd.getBinding().toString())+": "+Utilities.escapeXml(cd.getDescription())+"</td></tr>\r\n");
      }
    }
    s.append("</table>\r\n");
    return s.toString();
  }
  
  private String genBindingTable(boolean codelists) {
    StringBuilder s = new StringBuilder();
    s.append("<table class=\"codes\">\r\n");
    List<String> names = new ArrayList<String>();
    for (String n : definitions.getBindings().keySet()) {
      if ((codelists && definitions.getBindingByName(n).getBinding() == Binding.CodeList) || (!codelists && definitions.getBindingByName(n).getBinding() != Binding.CodeList))
       names.add(n);
    }
    Collections.sort(names);
    for (String n : names) {
      if (!n.startsWith("*")) {
        BindingSpecification cd = definitions.getBindingByName(n);
        if (cd.getBinding() == Binding.CodeList || cd.getBinding() == Binding.Special)
          s.append("  <tr><td title=\""+Utilities.escapeXml(cd.getDefinition())+"\">"+cd.getName()+"<br/><font color=\"grey\">http://hl7.org/fhir/sid/"+cd.getReference().substring(1)+"</font></td><td>");
        else
          s.append("  <tr><td title=\""+Utilities.escapeXml(cd.getDefinition())+"\">"+cd.getName()+"</td><td>");
        if (cd.getBinding() == Binding.Unbound) {
          s.append("Definition: "+Utilities.escapeXml(cd.getDefinition()));
        } else if (cd.getBinding() == Binding.CodeList) {
          if (cd.getBindingStrength() == BindingStrength.Preferred)
            s.append("Preferred codes: ");
          else if (cd.getBindingStrength() == BindingStrength.Suggested)
            s.append("Suggested codes: ");
          else // if (cd.getBindingStrength() == BindingStrength.Required)
            s.append("Required codes: ");
          s.append("    <table class=\"codes\">\r\n");
          boolean hasComment = false;
          boolean hasDefinition = false;
          for (DefinedCode c : cd.getCodes()) {
            hasComment = hasComment || c.hasComment();
            hasDefinition = hasDefinition || c.hasDefinition();
          }
          for (DefinedCode c : cd.getCodes()) {
            if (hasComment)
              s.append("    <tr><td>"+Utilities.escapeXml(c.getCode())+"</td><td>"+Utilities.escapeXml(c.getDefinition())+"</td><td>"+Utilities.escapeXml(c.getComment())+"</td></tr>");
            else if (hasDefinition)
              s.append("    <tr><td>"+Utilities.escapeXml(c.getCode())+"</td><td colspan=\"2\">"+Utilities.escapeXml(c.getDefinition())+"</td></tr>");
            else
              s.append("    <tr><td colspan=\"3\">"+Utilities.escapeXml(c.getCode())+"</td></tr>");
          }
          s.append("    </table>\r\n");
        } else if (cd.getBinding() == Binding.ValueSet) {
          if (cd.getBindingStrength() == BindingStrength.Preferred)
            s.append("Preferred codes: ");
          else if (cd.getBindingStrength() == BindingStrength.Suggested)
            s.append("Suggested codes: ");
          else // if (cd.getBindingStrength() == BindingStrength.Required)
            s.append("Required codes: ");
          if (cd.hasReference())
            s.append("<a href=\""+cd.getReference()+"\">Value Set "+cd.getDescription()+"</a>");
          else
            s.append("Value Set "+cd.getDescription());
        } else if (cd.getBinding() == Binding.Reference) {
          s.append("See <a href=\""+cd.getReference()+"\">"+cd.getReference()+"</a>");
        } else if (cd.getBinding() == Binding.Special) {
          if (cd.getName().equals("MessageEvent"))
            s.append("See the <a href=\"message.htm#Events\"> Event List </a>in the messaging framework");
          else if (cd.getName().equals("ResourceType"))
            s.append("See the <a href=\"terminologies.htm#ResourceType\"> list of defined Resource Types</a>");
          else if (cd.getName().equals("FHIRContentType"))
            s.append("See the <a href=\"terminologies.htm#fhircontenttypes\"> list of defined Resource and Data Types</a>");
          else 
            s.append("<a href=\"datatypes.htm\">Any defined data Type name</a> (including <a href=\"xml.htm#Resource\">Resource</a>)");
        }        
        s.append("</td></tr>\r\n");
      }
      
    }
    s.append("</table>\r\n");
    return s.toString();
  }

  private String getEventsTable() {
    List<String> codes = new ArrayList<String>();
    codes.addAll(definitions.getEvents().keySet());
    Collections.sort(codes);
    StringBuilder s = new StringBuilder();
    s.append("<table class=\"grid\">\r\n");
    s.append(" <tr><th>Code</th><th>Description</th><th>Request</th><th>Response</th><th>Notes</th></tr>\r\n");
    for (String c : codes) {
      EventDefn e = definitions.getEvents().get(c);
      if (e.getUsages().size() == 1) {
        EventUsage u = e.getUsages().get(0);
        s.append(" <tr><td>"+e.getCode()+"</td><td>"+e.getDefinition()+"</td>");
        s.append("<td>"+describeMsg(u.getRequestResources(), u.getRequestAggregations())+"</td><td>"+
            describeMsg(u.getResponseResources(), u.getResponseAggregations())+"</td><td>"+
            combineNotes(e.getFollowUps(), u.getNotes())+"</td></tr>\r\n");
      } else {
        boolean first = true;
        for (EventUsage u : e.getUsages()) {
          if (first)
            s.append(" <tr><td rowspan=\""+Integer.toString(e.getUsages().size())+"\">"+e.getCode()+"</td><td rowspan=\""+Integer.toString(e.getUsages().size())+"\">"+e.getDefinition()+"</td>");
          else
            s.append(" <tr>");
          first = false;
          s.append("<td>"+describeMsg(u.getRequestResources(), u.getRequestAggregations())+"</td><td>"+
              describeMsg(u.getResponseResources(), u.getResponseAggregations())+"</td><td>"+
              combineNotes(e.getFollowUps(), u.getNotes())+"</td></tr>\r\n");
        }
      }
    }
    s.append("</table>\r\n");
    return s.toString();
  }

  private String genResCodes() {
    StringBuilder html = new StringBuilder();
    List<String> names = new ArrayList<String>();
    names.addAll(definitions.getKnownResources().keySet());
    Collections.sort(names);
    for (String n : names) {
      DefinedCode c = definitions.getKnownResources().get(n);
      String htmlFilename = c.getComment();
      
      if( definitions.getFutureResources().containsKey(c.getCode()) )
    	  htmlFilename = "resources";
      
      html.append("  <tr><td><a href=\""+htmlFilename+".htm\">"+c.getCode()+"</a></td><td>"+Utilities.escapeXml(c.getDefinition())+"</td></tr>");
    }       
    return html.toString();
  }

  private String genDTCodes() {
    StringBuilder html = new StringBuilder();
    List<String> names = new ArrayList<String>();
    names.addAll(definitions.getTypes().keySet());
    names.addAll(definitions.getStructures().keySet());
    names.addAll(definitions.getInfrastructure().keySet());
    Collections.sort(names);
    for (String n : names) {
      if (!definitions.dataTypeIsSharedInfo(n)) {
        ElementDefn c = definitions.getTypes().get(n);
        if (c == null)
          c = definitions.getStructures().get(n);
        if (c == null)
          c = definitions.getInfrastructure().get(n);
        if (c.getName().equals("Extension"))
          html.append("  <tr><td><a href=\"extensibility.htm\">"+c.getName()+"</a></td><td>"+Utilities.escapeXml(c.getDefinition())+"</td></tr>");
        else if (c.getName().equals("DocumentInformation"))
          html.append("  <tr><td><a href=\"documentinformation.htm\">"+c.getName()+"</a></td><td>"+Utilities.escapeXml(c.getDefinition())+"</td></tr>");
        else if (c.getName().equals("Narrative") || c.getName().equals("ResourceReference") )
          html.append("  <tr><td><a href=\"xml.htm#"+c.getName()+"\">"+c.getName()+"</a></td><td>"+Utilities.escapeXml(c.getDefinition())+"</td></tr>");
        else
          html.append("  <tr><td><a href=\"datatypes.htm#"+c.getName()+"\">"+c.getName()+"</a></td><td>"+Utilities.escapeXml(c.getDefinition())+"</td></tr>");
      }       
    }
    return html.toString();
  }

  private String genResImplList() {
    StringBuilder html = new StringBuilder();
    List<String> res = new ArrayList<String>();
    for (ResourceDefn n: definitions.getResources().values())
      res.add(n.getName());
    for (DefinedCode c : definitions.getKnownResources().values()) {
      if (res.contains(c.getComment()))
        html.append("  <tr><td>"+c.getCode()+"</td><td><a href=\""+c.getComment()+".dict.xml\">Definitions</a></td><td><a href=\""+c.getComment()+".xsd\">Schema</a></td><td><a href=\""+c.getComment()+".xml\">Example</a></td><td><a href=\""+c.getComment()+".json\">JSON Example</a></td>\r\n");
    }       
    return html.toString();

  }

  private String genReferenceImplList() {
    StringBuilder s = new StringBuilder();
    for (PlatformGenerator gen : referenceImplementations) {
      s.append("<li><b><a href=\""+gen.getName()+".zip\">"+gen.getTitle()+"</a></b>: "+Utilities.escapeXml(gen.getDescription())+"</li>\r\n");
    }
    return s.toString();
  }


  String processPageIncludesForPrinting(String file, String src) throws Exception {
    while (src.contains("<%"))
    {
      int i1 = src.indexOf("<%");
      int i2 = src.indexOf("%>");
      String s1 = src.substring(0, i1);
      String s2 = src.substring(i1 + 2, i2).trim();
      String s3 = src.substring(i2+2);
      String name = file.substring(0,file.indexOf(".")); 

      String[] com = s2.split(" ");
      if (com.length == 2 && com[0].equals("dt"))
        src = s1+xmlForDt(com[1], null)+tsForDt(com[1])+s3;
      else if (com.length == 2 && com[0].equals("dt.constraints")) 
        src = s1+genConstraints(com[1])+s3;
      else if (com.length == 2 && com[0].equals("dt.restrictions")) 
        src = s1+genRestrictions(com[1])+s3;
      else if (com.length == 2 && com[0].equals("dictionary"))
        src = s1+dictForDt(com[1])+s3;
      else if (com[0].equals("pageheader") || com[0].equals("dtheader") || com[0].equals("xmlheader") || com[0].equals("extheader") || com[0].equals("txheader") || com[0].equals("atomheader"))
        src = s1+s3;
      else if (com[0].equals("resheader"))
        src = s1+resHeader(name, "Document", com.length > 1 ? com[1] : null)+s3;
      else if (com[0].equals("codelist"))
        src = s1+codelist(name, com.length > 1 ? com[1] : null)+s3;
      else if (com.length != 1)
        throw new Exception("Instruction <%"+s2+"%> not understood parsing page "+file);
      else if (com[0].equals("footer"))
        src = s1+TextFile.fileToString(folders.srcDir + "footer.htm")+s3;
      else if (com[0].equals("sidebar"))
        src = s1+s3;
      else if (com[0].equals("title"))
        src = s1+name.toUpperCase().substring(0, 1)+name.substring(1)+s3;
      else if (com[0].equals("name"))
        src = s1+name+s3;
      else if (com[0].equals("version"))
        src = s1+ini.getStringProperty("FHIR", "version")+s3;
      else if (com[0].equals("gendate"))
        src = s1+Config.DATE_FORMAT().format(new Date())+s3;
      else if (com[0].equals("maindiv"))
        src = s1+s3;
      else if (com[0].equals("/maindiv"))
        src = s1+s3;
      else if (com[0].equals("events"))
        src = s1 + getEventsTable()+ s3;
      else if (com[0].equals("resourcecodes"))
        src = s1 + genResCodes() + s3;
      else if (com[0].equals("datatypecodes"))
        src = s1 + genDTCodes() + s3;
      else if (com[0].equals("bindingtable-codelists"))
        src = s1 + genBindingTable(true) + s3;
      else if (com[0].equals("bindingtable"))
        src = s1 + genBindingsTable() + s3;
      else if (com[0].equals("bindingtable-others"))
        src = s1 + genBindingTable(false) + s3;
      else if (com[0].equals("codeslist"))
        src = s1 + genCodeSystemsTable() + s3;
      else if (com[0].equals("resimplall"))
        src = s1 + genResImplList() + s3;
      else if (com[0].equals("impllist"))
        src = s1 + genReferenceImplList() + s3;
      else if (com[0].equals("txurl"))
        src = s1 + "http://hl7.org/fhir/"+Utilities.fileTitle(file) + s3;
      else if (com[0].equals("txdef"))
        src = s1 + generateCodeDefinition(Utilities.fileTitle(file)) + s3;
      else if (com[0].equals("txusage"))
        src = s1 + generateBSUsage(Utilities.fileTitle(file)) + s3;
      else if (com[0].equals("txsummary"))
        src = s1 + generateCodeTable(Utilities.fileTitle(file)) + s3;
      else if (com[0].equals("toc"))
        src = s1 + generateToc() + s3;
      else 
        throw new Exception("Instruction <%"+s2+"%> not understood parsing page "+file);
    }
    return src;
  } 

  String processPageIncludesForBook(String file, String src) throws Exception {
    while (src.contains("<%"))
    {
      int i1 = src.indexOf("<%");
      int i2 = src.indexOf("%>");
      String s1 = src.substring(0, i1);
      String s2 = src.substring(i1 + 2, i2).trim();
      String s3 = src.substring(i2+2);
      String name = file.substring(0,file.indexOf(".")); 

      String[] com = s2.split(" ");
      if (com.length == 2 && com[0].equals("dt"))
        src = s1+xmlForDt(com[1], null)+tsForDt(com[1])+s3;
      else if (com.length == 2 && com[0].equals("dt.constraints")) 
        src = s1+genConstraints(com[1])+s3;
      else if (com.length == 2 && com[0].equals("dt.restrictions")) 
        src = s1+genRestrictions(com[1])+s3;
      else if (com.length == 2 && com[0].equals("dictionary"))
        src = s1+dictForDt(com[1])+s3;
      else if (com[0].equals("pageheader") || com[0].equals("dtheader") || com[0].equals("xmlheader") || com[0].equals("extheader") || com[0].equals("txheader") || com[0].equals("atomheader"))
        src = s1+s3;
      else if (com[0].equals("resheader"))
        src = s1+s3;
      else if (com[0].equals("codelist"))
        src = s1+codelist(name, com.length > 1 ? com[1] : null)+s3;
      else if (com[0].equals("onthispage"))
        src = s1+onThisPage(s2.substring(com[0].length()+1))+s3;
      else if (com.length != 1)
        throw new Exception("Instruction <%"+s2+"%> not understood parsing page "+file);
      else if (com[0].equals("footer"))
        src = s1+s3;
      else if (com[0].equals("sidebar"))
        src = s1+s3;
      else if (com[0].equals("title"))
        src = s1+name.toUpperCase().substring(0, 1)+name.substring(1)+s3;
      else if (com[0].equals("name"))
        src = s1+name+s3;
      else if (com[0].equals("version"))
        src = s1+ini.getStringProperty("FHIR", "version")+s3;
      else if (com[0].equals("gendate"))
        src = s1+Config.DATE_FORMAT().format(new Date())+s3;
      else if (com[0].equals("maindiv"))
        src = s1+s3;
      else if (com[0].equals("/maindiv"))
        src = s1+s3;
      else if (com[0].equals("events"))
        src = s1 + getEventsTable()+ s3;
      else if (com[0].equals("resourcecodes"))
        src = s1 + genResCodes() + s3;
      else if (com[0].equals("datatypecodes"))
        src = s1 + genDTCodes() + s3;
      else if (com[0].equals("bindingtable-codelists"))
        src = s1 + genBindingTable(true) + s3;
      else if (com[0].equals("codeslist"))
        src = s1 + genCodeSystemsTable() + s3;
      else if (com[0].equals("bindingtable"))
        src = s1 + genBindingsTable() + s3;
      else if (com[0].equals("bindingtable-others"))
        src = s1 + genBindingTable(false) + s3;
      else if (com[0].equals("resimplall"))
        src = s1 + genResImplList() + s3;
      else if (com[0].equals("impllist"))
        src = s1 + genReferenceImplList() + s3;
      else if (com[0].equals("txurl"))
        src = s1 + "http://hl7.org/fhir/"+Utilities.fileTitle(file) + s3;
      else if (com[0].equals("txdef"))
        src = s1 + generateCodeDefinition(Utilities.fileTitle(file)) + s3;
      else if (com[0].equals("txusage"))
        src = s1 + generateBSUsage(Utilities.fileTitle(file)) + s3;
      else if (com[0].equals("txsummary"))
        src = s1 + generateCodeTable(Utilities.fileTitle(file)) + s3;
      else if (com[0].equals("toc"))
        src = s1 + generateToc() + s3;
      else 
        throw new Exception("Instruction <%"+s2+"%> not understood parsing page "+file);
    }
    return src;
  } 



  String processResourceIncludes(String name, ResourceDefn resource, String xml, String tx, String dict, String src) throws Exception {
    while (src.contains("<%"))
    {
      int i1 = src.indexOf("<%");
      int i2 = src.indexOf("%>");
      String s1 = src.substring(0, i1);
      String s2 = src.substring(i1 + 2, i2).trim();
      String s3 = src.substring(i2+2);

      String[] com = s2.split(" ");
      if (com[0].equals("resheader"))
        src = s1+resHeader(name, resource.getName(), com.length > 1 ? com[1] : null)+s3;
      else if (com.length != 1)
        throw new Exception("Instruction <%"+s2+"%> not understood parsing resource "+name);
      else if (com[0].equals("pageheader"))
        src = s1+pageHeader(resource.getName())+s3;
      else if (com[0].equals("footer"))
        src = s1+TextFile.fileToString(folders.srcDir + "footer.htm")+s3;
      else if (com[0].equals("sidebar"))
        src = s1+generateSideBar()+s3;
      else if (com[0].equals("title"))
        src = s1+resource.getName()+s3;
      else if (com[0].equals("status"))
        src = s1+resource.getStatus()+s3;
      else if (com[0].equals("introduction")) 
        src = s1+loadXmlNotes(name, "introduction")+s3;
      else if (com[0].equals("examples")) 
        src = s1+produceExamples(resource)+s3;
      else if (com[0].equals("profiles")) 
        src = s1+produceProfiles(resource)+s3;
      else if (com[0].equals("example-list")) 
        src = s1+produceExampleList(resource)+s3;
      else if (com[0].equals("examples-book")) 
        src = s1+produceBookExamples(resource)+s3;
      else if (com[0].equals("name"))
        src = s1+name+s3;
      else if (com[0].equals("search"))
        src = s1+getSearch(resource)+s3;
      else if (com[0].equals("version"))
        src = s1+ini.getStringProperty("FHIR", "version")+s3;
      else if (com[0].equals("gendate"))
        src = s1+Config.DATE_FORMAT().format(new Date())+s3;
      else if (com[0].equals("definition"))
        src = s1+resource.getRoot().getDefinition()+s3;
      else if (com[0].equals("xml"))
        src = s1+xml+s3;
      else if (com[0].equals("tx"))
        src = s1+tx+s3;
      else if (com[0].equals("inv"))
        src = s1+genResourceConstraints(resource)+s3;
      else if (com[0].equals("plural"))
        src = s1+Utilities.pluralizeMe(name)+s3;
      else if (com[0].equals("notes")) {
        src = s1+loadXmlNotes(name, "notes")+s3;
      } else if (com[0].equals("dictionary"))
        src = s1+dict+s3;
      else if (com[0].equals("resurl")) {
        if (isAggregationEndpoint(resource.getName()))
          src = s1+s3;
        else
          src = s1+"<p>The resource name as it appears in a <a href=\"http.htm\"> RESTful URL</a> is /"+name+"/</p>"+s3;
      } else 
        throw new Exception("Instruction <%"+s2+"%> not understood parsing resource "+name);

    }
    return src;
  }

 

  private String getSearch(ResourceDefn resource) {
    if (resource.getSearchParams().size() == 0)
      return "";
    else {
      StringBuilder b = new StringBuilder();
      b.append("<h2>Search Parameters</h2>\r\n");
      b.append("<p>Search Parameters for RESTful searches. The standard parameters also apply. See <a href=\"http.htm#search\">Searching</a> for more information.</p>\r\n");
      b.append("<table class=\"list\">\r\n");
      for (SearchParameter p : resource.getSearchParams()) {
        if (p.getType() == SearchType.date) {
          b.append("<tr><td>"+p.getCode()+" : "+p.getType()+"</td><td>date equal to "+Utilities.escapeXml(p.getDescription())+"</td><td>single</td></tr>\r\n");
          b.append("<tr><td>"+p.getCode()+"-before : "+p.getType()+"</td><td>date before or equal to "+Utilities.escapeXml(p.getDescription())+"</td><td>single</td></tr>\r\n");
          b.append("<tr><td>"+p.getCode()+"-after : "+p.getType()+"</td><td>date after or equal to "+Utilities.escapeXml(p.getDescription())+"</td><td>single</td></tr>\r\n");          
        } else
          b.append("<tr><td>"+p.getCode()+" : "+p.getType()+"</td><td>"+Utilities.escapeXml(p.getDescription())+"</td><td>"+p.getRepeatMode().toString()+"</td></tr>\r\n");
      }
      b.append("</table>\r\n");
      b.append("<p>(See <a href=\"http.htm#search\">Searching</a>).</p>\r\n");
      return b.toString();
    }
  }

  private String produceExamples(ResourceDefn resource) {
    StringBuilder s = new StringBuilder();
    for (Example e: resource.getExamples()) {
        s.append("<tr><td>"+Utilities.escapeXml(e.getDescription())+"</td><td><a href=\""+e.getFileTitle()+".xml\">source</a></td><td><a href=\""+e.getFileTitle()+".xml.htm\">formatted</a></td></tr>");
    }
    return s.toString();
  }

  private String produceProfiles(ResourceDefn resource) {
    StringBuilder s = new StringBuilder();
    for (RegisteredProfile p: resource.getProfiles()) {
        s.append("<tr><td><a href=\""+p.getFilename()+".htm\">"+Utilities.escapeXml(p.getName())+"</a></td><td>"+Utilities.escapeXml(p.getDescription())+"</td></tr>");
    }
    return s.toString();
  }

  private String produceExampleList(ResourceDefn resource) {
    StringBuilder s = new StringBuilder();
    boolean started = false;
    for (Example e: resource.getExamples()) {
      if (!e.isInBook()) {
        if (!started)
          s.append("<p>Additional Examples:</p>\r\n<table class=\"list\">\r\n");
        started = true;
        s.append("<tr><td>"+Utilities.escapeXml(e.getDescription())+"</td>");
        s.append("<td><a href=\""+e.getFileTitle()+".xml\">XML</a></td><td><a href=\""+e.getFileTitle()+".xml.htm\">(for browser)</a></td>");
        s.append("<td><a href=\""+e.getFileTitle()+".json\">JSON</a></td><td><a href=\""+e.getFileTitle()+".json.htm\">(for browser)</a></td>");
        s.append("</tr>");
      }
    }
    if (started)
      s.append("</table>\r\n");
    return s.toString();
  }

  
    
  private String produceBookExamples(ResourceDefn resource) {
    StringBuilder s = new StringBuilder();
    for (Example e: resource.getExamples()) {
      if (e.isInBook()) {
        s.append("<h3>"+Utilities.escapeXml(e.getName())+"</h3>\r\n");
        s.append("<p>"+Utilities.escapeXml(e.getDescription())+"</p>\r\n");
        s.append(e.getXhtm());
        s.append("<p>JSON Equivalent</p>\r\n");
        s.append(e.getJson());
      }
    }
    return s.toString();
  }

  private static final String HTML_PREFIX = "<div xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/1999/xhtml ../../schema/xhtml1-strict.xsd\" xmlns=\"http://www.w3.org/1999/xhtml\">\r\n";
  private static final String HTML_SUFFIX = "</div>\r\n";
  
  private String loadXmlNotes(String name, String suffix) throws Exception {
    String filename;
    if (new CSFile(folders.sndBoxDir + name).exists())
      filename = folders.sndBoxDir + name+File.separatorChar+name+"-"+suffix+".xml";
    else
      filename = folders.srcDir + name+File.separatorChar+name+"-"+suffix+".xml";
    
    if (!new CSFile(filename).exists()) {
      TextFile.stringToFile(HTML_PREFIX+"\r\n<!-- content goes here -->\r\n\r\n"+HTML_SUFFIX, filename);
      return "";
    }
    
    String cnt = TextFile.fileToString(filename);
    cnt = processPageIncludes(filename, cnt).trim()+"\r\n";
    if (cnt.startsWith("<div")) {
      if (!cnt.startsWith(HTML_PREFIX))
        throw new Exception("unable to process start xhtml content "+name+" : "+cnt.substring(0, HTML_PREFIX.length()));
      else if (!cnt.endsWith(HTML_SUFFIX))
        throw new Exception("unable to process end xhtml content "+name+" : "+cnt.substring(cnt.length()-HTML_SUFFIX.length()));
      else {
        String res = cnt.substring(HTML_PREFIX.length(), cnt.length()-(HTML_SUFFIX.length()));
        return res;
      }
    } else {
      TextFile.stringToFile(HTML_PREFIX+cnt+HTML_SUFFIX, filename);
      return cnt;
    }
}

  String processProfileIncludes(String filename, ProfileDefn profile, String xml, String tx, String src) throws Exception {
    while (src.contains("<%"))
    {
      int i1 = src.indexOf("<%");
      int i2 = src.indexOf("%>");
      String s1 = src.substring(0, i1);
      String s2 = src.substring(i1 + 2, i2).trim();
      String s3 = src.substring(i2+2);

      String[] com = s2.split(" ");
      if (com.length != 1)
        throw new Exception("Instruction <%"+s2+"%> not understood parsing resource "+filename);
      else if (com[0].equals("pageheader"))
        src = s1+pageHeader(profile.getMetadata().get("name").get(0))+s3;
      else if (com[0].equals("footer"))
        src = s1+TextFile.fileToString(folders.srcDir + "footer.htm")+s3;
      else if (com[0].equals("sidebar"))
        src = s1+generateSideBar()+s3;
      else if (com[0].equals("title"))
        src = s1+profile.getMetadata().get("name").get(0)+s3;
      else if (com[0].equals("name"))
        src = s1+filename+s3;
      else if (com[0].equals("date")) {
        Date d = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(profile.getMetadata().get("date").get(0));
        src = s1+Config.DATE_FORMAT().format(d)+s3;
      } else if (com[0].equals("version"))
        src = s1+ini.getStringProperty("FHIR", "version")+s3;
      else if (com[0].equals("gendate"))
        src = s1+Config.DATE_FORMAT().format(new Date())+s3;
      else if (com[0].equals("definition"))
        src = s1+profile.getMetadata().get("description").get(0)+s3;
      else if (com[0].equals("status"))
        src = s1+describeStatus(profile.getMetadata().get("status").get(0))+s3;
      else if (com[0].equals("author"))
        src = s1+profile.getMetadata().get("author.name").get(0)+s3;
      else if (com[0].equals("xml"))
        src = s1+xml+s3;
      else if (com[0].equals("profilelist")) {
        if (profile.getMetadata().containsKey("resource"))
          src = s1+"profiles the "+profile.getMetadata().get("resource").get(0)+" Resource"+s3;
        else
          src = s1+s3;
      } else if (com[0].equals("tx"))
        src = s1+tx+s3;
      else if (com[0].equals("inv"))
        src = s1+(profile.getResources().size() == 0 ? "" : genProfileConstraints(profile.getResources().get(0)))+s3;      
      else if (com[0].equals("plural"))
        src = s1+Utilities.pluralizeMe(filename)+s3;
      else if (com[0].equals("notes"))
        src = s1+"todo" /*Utilities.fileToString(folders.srcDir + filename+File.separatorChar+filename+".htm")*/ +s3;
      else if (com[0].equals("dictionary"))
        src = s1+"todo"+s3;
      else if (com[0].equals("resurl")) {
          src = s1+"The id of this profile is "+profile.getMetadata().get("id").get(0)+s3;
      } else 
        throw new Exception("Instruction <%"+s2+"%> not understood parsing resource "+filename);
    }
    return src;
  }

  private boolean isAggregationEndpoint(String name) {
    return definitions.getAggregationEndpoints().contains(name.toLowerCase());
  }   


  private String describeStatus(String s) {
    if (s.equals("draft"))
      return "as a draft";
    if (s.equals("testing"))
      return "for testing";
    if (s.equals("production"))
      return "for production use";
    if (s.equals("withdrawn"))
      return "as withdrawn from use";
    if (s.equals("superceded"))
      return "as superceded";
    return "with unknown status '" +s+'"';
  }

  public Definitions getDefinitions() {
    return definitions;
  }

  public FolderManager getFolders() {
    return folders;
  }
  public String getVersion() {
    return version;
  }

  public Navigation getNavigation() {
    return navigation;
  }

  public List<PlatformGenerator> getReferenceImplementations() {
    return referenceImplementations;
  }

  public IniFile getIni() {
    return ini;
  }

  public void setDefinitions(Definitions definitions) {
    this.definitions = definitions;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setFolders(FolderManager folders) {
    this.folders = folders;
  }

  public void setIni(IniFile ini) {
    this.ini = ini;
  }

  public Calendar getGenDate() {
    return genDate;
  }

  public void log(String content) {
//    if (notime) {
//      System.out.println(content);
//      notime = false;
//    } else {
      Date stop = new Date();
      long l1 = start.getTime();
      long l2 = stop.getTime();
      long diff = l2 - l1;
      long secs = diff / 1000;
      MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
      // mem.gc();
      long used = mem.getHeapMemoryUsage().getUsed() / (1024 * 1024);
      System.out.println(String.format("%1$-74s", content)+" "+String.format("%1$3s", Long.toString(secs))+"sec "+String.format("%1$4s", Long.toString(used))+"MB");
//    }
  }
  
//  public void logNoEoln(String content) {
//    System.out.print(content);  
//    notime = true;
//  }

  
  public void setNavigation(Navigation navigation) {
    this.navigation = navigation;
  }

  public List<String> getOrderedResources() {
    return orderedResources;
  }

  public Map<String, SectionTracker> getSectionTrackerCache() {
    return sectionTrackerCache;
  }

  public Map<String, TocEntry> getToc() {
    return toc;
  }

}
