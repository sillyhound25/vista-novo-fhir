package org.hl7.fhir.tools.publisher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlComposer;
import org.hl7.fhir.utilities.xhtml.XhtmlDocument;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.utilities.xhtml.XhtmlParser;

public class BookMaker {

  private PageProcessor page;
  private ChmMaker chm;
  private String target;
  
  private Map<String, XhtmlDocument> pages = new HashMap<String, XhtmlDocument>();

  private class RefTarget {
    XhtmlNode parent;
    int index;
  }

  
  
  public BookMaker(PageProcessor page, ChmMaker chm) {
    super();
    this.page = page;
    this.chm = chm;
  }

  private void produceBookForm() throws FileNotFoundException, Exception {
    target = page.getFolders().dstDir;
    target = target + File.separator;
    
    String src = TextFile.fileToString(page.getFolders().srcDir+"book.htm");
    src = page.processPageIncludes(page.getFolders().srcDir+"book.htm", src);
    XhtmlDocument doc = new XhtmlParser().parse(src);
    XhtmlNode body = doc.getElement("html").getElement("body");
    addTOC(body);   
    addContent(body);
    addReferenceIds(body);
    new XhtmlComposer().compose(new FileOutputStream(target+"fhir-book.htm"), doc); 
  }

  private void addReferenceIds(XhtmlNode body) {
    Map<String, RefTarget> tgts = new HashMap<String, RefTarget>();
    List<XhtmlNode> refs = new ArrayList<XhtmlNode>();
    buildIndex(refs, tgts, body, false);
    for (XhtmlNode a : refs) {
      if (a.getAttributes().get("href").startsWith("#")) {
        RefTarget r = tgts.get(a.getAttributes().get("href").substring(1));
        if (r != null) {
          int n = r.index + 1;
          while (n < r.parent.getChildNodes().size() && r.parent.getChildNodes().get(n).getNodeType() != NodeType.Element)
            n++;
          if (n < r.parent.getChildNodes().size()) {
            XhtmlNode h = r.parent.getChildNodes().get(n);
            if (h.getName().startsWith("h")) {
              String s = h.allText();
              if (s.contains(":"))
                a.addText(" (�"+s.substring(0, s.indexOf(':'))+")");
            }
            
          }
        }
        else {
          page.log("unable to resolve reference to "+a.getAttributes().get("href").substring(1)+" on "+a.allText());
          a.addText(" (Known Broken Link - needs to be resolved)");
        }
      }
    }    
  }

  private void buildIndex(List<XhtmlNode> refs, Map<String, RefTarget> tgts, XhtmlNode focus, boolean started) {
    int i = 0;
    for (XhtmlNode child : focus.getChildNodes()) {
      if (started) {
        if ("a".equals(child.getName()) && child.getAttributes().containsKey("name")) {
          RefTarget r = new RefTarget();
          r.parent = focus;
          r.index = i;
          tgts.put(child.getAttributes().get("name"), r);
        }
        if ("a".equals(child.getName()) && child.getAttributes().containsKey("href")) {
          //System.out.println("found "+child.getAttributes().get("href"));
          refs.add(child);
        }

        if (child.getNodeType() == NodeType.Element && !child.getName().equals("pre"))
          buildIndex(refs, tgts, child, true);
      } else if ("hr".equals(child.getName()))
        started = true;
      i++;
    }
    
  }

  private class LevelCounter {
    int l1;
    int l2;
    int l3;
    int l4;
  }
  private void addContent(XhtmlNode body) throws Exception {
    XhtmlNode e = body.getElement("contents");
    XhtmlNode div = body.addTag(body.getChildNodes().indexOf(e), "div");
    body.getChildNodes().remove(e);

    LevelCounter lvl = new LevelCounter();
    lvl.l1 = 0;
    for (Navigation.Category s : page.getNavigation().getCategories()) {
      lvl.l1++;
//      div.addTag("div").setAttribute("class", "page-break");
      XhtmlNode divS = div.addTag("div");
      divS.attribute("class", "section");
      XhtmlNode h1 = divS.addTag("h1");
      h1.addText(Integer.toString(lvl.l1)+": "+s.getName());

      lvl.l2 = 0;
      for (Navigation.Entry n : s.getEntries()) {
        lvl.l2++;
        lvl.l3 = 0;
        if (n.getLink() != null) {
          addPageContent(lvl, divS, n);
        }
        for (Navigation.Entry g : n.getEntries()) {
          if (g.getLink() != null) {
            addPageContent(lvl, divS, g);
          }
        }
      }
    }
  }

  private void addPageContent(LevelCounter lvl, XhtmlNode divS, Navigation.Entry n) throws Exception, Error {
    XhtmlNode divT = divS.addTag("div");
    XhtmlNode a = divT.addTag("a");
    a.attribute("name", n.getLink());
    a.addText(" "); // work around for a browser bug

    boolean first = true;
    XhtmlDocument page = pages.get(n.getLink()+".htm");
    if (page == null)
      throw new Exception("No content found for "+n.getLink()+".htm");
    if (page.getElement("html") == null)
      throw new Exception("No 'html' tag found in "+n.getLink()+".htm");
    if (page.getElement("html").getElement("body") == null)
      throw new Exception("No 'body' tag found in "+n.getLink()+".htm");
    XhtmlNode pageBody = page.getElement("html").getElement("body");
    for (XhtmlNode child : pageBody.getChildNodes()) {
      if (child.getNodeType() == NodeType.Element) {
        fixReferences(pageBody, child, n.getLink());
      }
      if ("h1".equals(child.getName())) {
        if (!first)
          throw new Error("?? ("+n.getLink()+".h1 repeated) ");
        first = false;
        chm.registerKeyWord(child.allText(), n.getLink()+".htm", n.getName());
        child.setName("h2");
        child.addText(0, Integer.toString(lvl.l1)+"."+Integer.toString(lvl.l2)+": ");

      } else if ("h2".equals(child.getName())) {
        lvl.l3++;
        lvl.l4 = 0;
        chm.registerKeyWord(child.allText(), n.getLink()+".htm", n.getName());
        child.setName("h3");
        child.addText(0, Integer.toString(lvl.l1)+"."+Integer.toString(lvl.l2)+"."+Integer.toString(lvl.l3)+": ");
      } else if ("h3".equals(child.getName())) {
        lvl.l4++;
        chm.registerKeyWord(child.allText(), n.getLink()+".htm", n.getName());
        child.setName("h4");
        child.addText(0, Integer.toString(lvl.l1)+"."+Integer.toString(lvl.l2)+"."+Integer.toString(lvl.l3)+"."+Integer.toString(lvl.l4)+": ");
      } else if ("h4".equals(child.getName())) {
        child.setName("h5");
      }
    }
//        if (i2 != 1)
//          divT.addTag("div").setAttribute("class", "page-break");
    divT.getChildNodes().addAll(pageBody.getChildNodes());
  }

  
  private void fixReferences(XhtmlNode parent, XhtmlNode node, String name) throws Exception {
    for (XhtmlNode child : node.getChildNodes()) {
      if (child.getNodeType() == NodeType.Element) {
        fixReferences(node, child, name);
      }    
    }
    if (node.getName().equals("a")) {      
      if (node.getAttributes().containsKey("name")) {
        String lname = node.getAttributes().get("name");
        node.getAttributes().put("name", name+"."+lname);
        //System.out.println("found anchor "+name+"."+lname);
      } else { 
        String s = node.getAttributes().get("href");
        if (s == null || s.length() == 0)
          throw new Error("empty \"a\" tag");
        if (s.startsWith("#")) {
          s = "#"+name+"."+s.substring(1);
        } else if (s.startsWith("http:") || s.startsWith("https:")) {
          s = s;
        } else {
          int i = s.indexOf('.');
          if (i == -1)
            throw new Error("unable to understand ref: '"+s+"'");

          if (s.contains("#")) {
            int j = s.indexOf('#');
            s = "#"+s.substring(0, i)+"."+s.substring(j+1);
          } else if (s.endsWith(".htm")) {
            s = "#"+s.substring(0, i);
          } else {
            if (!s.endsWith(".zip") && !s.endsWith(".xsd") && !s.endsWith(".png") && !s.endsWith(".xml") && !s.endsWith(".eap") && !s.endsWith(".xmi")) {
              System.out.println("odd ref: "+s+" in "+node.allText());
              s = s;
            } else {
              // actually, what we want to do is do what?
//              System.out.println("ref to remove: "+s+" in "+node.allText());
//              Utilities.copyFile(new File(page.getFolders().dstDir+s), new File(targetBin+File.separatorChar+s));
//              s = "http://hl7.org/documentcenter/public/standards/FHIR/v"+page.getVersion()+"/"+s;
            }
          }

        }
        node.getAttributes().put("href", s);
        if (s.startsWith("http") && parent != null && !node.allText().equals(s)) {
          node.addText(" ("+s+") ");
        }
        //System.out.println("reference to "+s); 
      }
    }
  }

  private void addTOC(XhtmlNode body) {
    XhtmlNode e = body.getElement("index");
    XhtmlNode div = body.addTag(body.getChildNodes().indexOf(e), "div");
    body.getChildNodes().remove(e);
    div.attribute("class", "toc");
    div.addText("\r\n  ");
    int i1 = 0;
    XhtmlNode p = div.addTag("p");
    p.addText("\r\n    ");
    for (Navigation.Category c : page.getNavigation().getCategories()) {
      i1++;
      p.addText(Integer.toString(i1)+": "+c.getName());
      p.addTag("br");
      p.addText("\r\n      ");
      int i2 = 0;
      for (Navigation.Entry n : c.getEntries()) {
        if (n.getLink() != null) {
          i2++;
          p.addText(XhtmlNode.NBSP+XhtmlNode.NBSP+Integer.toString(i1)+"."+Integer.toString(i2)+": ");

          XhtmlNode a = p.addTag("a");
          a.attribute("href", "#"+n.getLink());
          a.addText(n.getName());
          p.addTag("br");
          p.addText("\r\n     ");
        }
      }
    }
    div.addText("\r\n  ");
  }

  public void produce() throws FileNotFoundException, Exception {
    produceBookForm(); 
    
  }

  public Map<String, XhtmlDocument> getPages() {
    return pages;
  }


  
}
