package org.hl7.fhir.instance.validation;

  
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
  
import org.hl7.fhir.instance.formats.XmlParser;
import org.hl7.fhir.instance.model.AtomEntry;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Profile;
import org.hl7.fhir.instance.model.Profile.BindingConformance;
import org.hl7.fhir.instance.model.Profile.BindingType;
import org.hl7.fhir.instance.model.Profile.ElementComponent;
import org.hl7.fhir.instance.model.Profile.ProfileBindingComponent;
import org.hl7.fhir.instance.model.Profile.ProfileStructureComponent;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.Type;
import org.hl7.fhir.instance.model.Uri;
import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.model.ValueSet.ValueSetDefineConceptComponent;
import org.hl7.fhir.instance.model.ValueSet.ValueSetExpansionContainsComponent;
import org.hl7.fhir.instance.utils.ValueSetExpansionCache;
import org.hl7.fhir.instance.validation.ValidationMessage.Source;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.xml.XMLUtil;
import org.w3c.dom.Element;
  

/* 
 * todo:
 * check urn's don't start oid: or uuid: 
 */
public class InstanceValidator extends BaseValidator {
    private static final String NS_FHIR = "http://hl7.org/fhir";
    
    private Map<String, Profile> types = new HashMap<String, Profile>();
    private Map<String, ValueSet> valuesets = new HashMap<String, ValueSet>();
    private Map<String, ValueSet> codesystems = new HashMap<String, ValueSet>();
    private ValueSetExpansionCache cache = new ValueSetExpansionCache(valuesets, codesystems);
    private boolean suppressLoincSnomedMessages;
    
    public InstanceValidator(String validationZip) throws Exception {
      super();
      source = Source.InstanceValidator;
      loadValidationResources(validationZip);
    }  
    
    private void loadValidationResources(String name) throws Exception {
      ZipInputStream zip = new ZipInputStream(new FileInputStream(name));
      ZipEntry ze;
      while ((ze = zip.getNextEntry()) != null) {
        if (ze.getName().endsWith(".xml")) {
          readFile(zip, ze.getName());
        }
        zip.closeEntry();
      }
      zip.close();    
    }
  
    public InstanceValidator(Map<String, byte[]> source) throws Exception {
      super();
      super.source = Source.InstanceValidator;
      loadValidationResources(source);
    }  
    
    private void loadValidationResources(Map<String, byte[]> source) throws Exception {
      for (String name : source.keySet()) {
        if (name.endsWith(".xml")) {
          readFile(new ByteArrayInputStream(source.get(name)), name);        
        }
      }
    }
    
    private void readFile(InputStream zip, String name) throws Exception {
      XmlParser xml = new XmlParser();
      AtomFeed f = xml.parseGeneral(zip).getFeed();
      for (AtomEntry e : f.getEntryList()) {
        if (e.getId() == null) {
          System.out.println("unidentified resource "+e.getLinks().get("self")+" in "+name);
        }
        Resource r = e.getResource();
        if (r instanceof Profile) {
          Profile p = (Profile) r;
          if (p.getStructure().get(0).getName() != null)
            types.put(p.getStructure().get(0).getNameSimple().toLowerCase(), p);
          else 
            types.put(p.getStructure().get(0).getTypeSimple().toLowerCase(), p);
        }
        if (r instanceof ValueSet) {
          ValueSet vs = (ValueSet) r;
          valuesets.put(vs.getIdentifierSimple(), vs);
          if (vs.getDefine() != null) {
            codesystems.put(vs.getDefine().getSystemSimple().toString(), vs);
          }
        }
      }
    }
  
  
    public class ChildIterator {
      private Element parent;
      private String basePath;
      private int lastCount;
      private Element child;
      
      public ChildIterator(String path, Element elem) {
        parent = elem;
        basePath = path;  
      }
  
      public boolean next() {
        if (child == null) { 
          child = XMLUtil.getFirstChild(parent);
          lastCount = 0;
        } else {
          String lastName = child.getLocalName();
          child = XMLUtil.getNextSibling(child);
          if (child != null && child.getLocalName().equals(lastName)) 
            lastCount++;
          else
            lastCount = 0;
        }
        return child != null;
      }
  
      public String name() {
        return child.getLocalName();
      }
  
      public Element element() {
        return child;
      }
  
      public String path() {
        String sfx = "";
        Element n = XMLUtil.getNextSibling(child);
        if (n != null && n.getLocalName().equals(child.getLocalName())) { 
          sfx = "["+Integer.toString(lastCount)+"]";
        }
        return basePath+"/f:"+name()+sfx;
      }
    }
  
    public void validateInstance(List<ValidationMessage> errors, Element elem) throws Exception {
      boolean feedHasAuthor = XMLUtil.getNamedChild(elem, "author") != null;
      if (elem.getLocalName().equals("feed")) {
        ChildIterator ci = new ChildIterator("", elem);
        while (ci.next()) {
          if (ci.name().equals("category"))
            validateTag(ci.path(), ci.element(), false);
          else if (ci.name().equals("id"))
            validateId(errors, ci.path(), ci.element(), true);
          else if (ci.name().equals("link"))
            validateLink(errors, ci.path(), ci.element(), false);
          else if (ci.name().equals("entry")) 
            validateAtomEntry(errors, ci.path(), ci.element(), feedHasAuthor);
        }
      }
      else
        validate(errors, "", elem);
    }
  
    public List<ValidationMessage> validateInstance(Element elem) throws Exception {
      List<ValidationMessage> errors = new ArrayList<ValidationMessage>();
      validateInstance(errors, elem);      
      return errors;
    }
  
    private void validateAtomEntry(List<ValidationMessage> errors, String path, Element element, boolean feedHasAuthor) throws Exception {
      rule(errors, "invalid", path, XMLUtil.getNamedChild(element, "title") != null, "Entry must have a title");
      rule(errors, "invalid", path, XMLUtil.getNamedChild(element, "updated") != null, "Entry must have a last updated time");
      rule(errors, "invalid", path, feedHasAuthor || XMLUtil.getNamedChild(element, "author") != null, "Entry must have an author because the feed doesn't");
      
      
      
      ChildIterator ci = new ChildIterator(path, element);
      while (ci.next()) {
        if (ci.name().equals("category"))
          validateTag(ci.path(), ci.element(), true);
        else if (ci.name().equals("id"))
          validateId(errors, ci.path(), ci.element(), true);
        else if (ci.name().equals("link"))
          validateLink(errors, ci.path(), ci.element(), true);
        else if (ci.name().equals("content")) {
          Element r = XMLUtil.getFirstChild(ci.element());
          validate(errors, ci.path()+"/f:"+r.getLocalName(), r);
        }
      }
    }
  
    private void validate(List<ValidationMessage> errors, String path, Element elem) throws Exception {
      if (elem.getLocalName().equals("Binary"))
        validateBinary(elem);
      else {
        Profile p = getProfileForType(elem.getLocalName());
        ProfileStructureComponent s = getStructureForType(p, elem.getLocalName());
        if (rule(errors, "invalid", elem.getLocalName(), s != null, "Unknown Resource Type "+elem.getLocalName())) {
          validateElement(errors, p, s, path+"/f:"+elem.getLocalName(), s.getElement().get(0), null, null, elem);
        }
      }
    }
  
    private Profile getProfileForType(String localName) throws Exception {
      Profile r = (Profile) getResource(localName);
      if (r == null)
        return null;
      if (r.getStructure().size() != 1 || !(r.getStructure().get(0).getTypeSimple().equals(localName) || r.getStructure().get(0).getNameSimple().equals(localName)))
        throw new Exception("unexpected profile contents");
      ProfileStructureComponent s = r.getStructure().get(0);
      return r;
    }
    
    private ProfileStructureComponent getStructureForType(Profile r, String localName) throws Exception {
      if (r.getStructure().size() != 1 || !(r.getStructure().get(0).getTypeSimple().equals(localName) || r.getStructure().get(0).getNameSimple().equals(localName)))
        throw new Exception("unexpected profile contents");
      ProfileStructureComponent s = r.getStructure().get(0);
      return s;
    }
  
    private Resource getResource(String id) {
      return types.get(id.toLowerCase());
    }
  
    private void validateBinary(Element elem) {
      // nothing yet
      
    }
  
    private void validateTag(String path, Element element, boolean onEntry) {
      // nothing yet
      
    }
  
    private void validateLink(List<ValidationMessage> errors, String path, Element element, boolean onEntry) {
      if (rule(errors, "invalid", path, element.hasAttribute("rel"), "Link element has no '@rel'")) {
        String rel = element.getAttribute("rel");
        if (rule(errors, "invalid", path, !Utilities.noString(rel), "Link/@rel is empty")) {
          if (rel.equals("self")) {
            if (rule(errors, "invalid", path, element.hasAttribute("href"), "Link/@rel='self' has no href"))
              rule(errors, "invalid", path, isAbsoluteUrl(element.getAttribute("href")), "Link/@rel='self' '"+element.getAttribute("href")+"' is not an absolute URI (must start with http:, https:, urn:, cid:");
          }
        }
      }
    }

    private void validateId(List<ValidationMessage> errors, String path, Element element, boolean onEntry) {
      if (rule(errors, "invalid", path, !Utilities.noString(element.getTextContent()), "id is empty"))
        rule(errors, "invalid", path, isAbsoluteUrl(element.getTextContent()), "Id '"+element.getTextContent()+"' is not an absolute URI (must start with http:, https:, urn:, cid:");
    }

    private boolean isAbsoluteUrl(String url) {
      if (url == null)
        return false;
      if (url.startsWith("http:"))
        return true;
      if (url.startsWith("https:"))
        return true;
      if (url.startsWith("urn:"))
        return true;
      if (url.startsWith("cid:"))
        return true;
      return false;
    }

    private void validateElement(List<ValidationMessage> errors, Profile profile, ProfileStructureComponent structure, String path, ElementComponent definition, Profile cprofile, ElementComponent context, Element element) throws Exception {
      // irrespective of what element it is, it cannot be empty
      if (NS_FHIR.equals(element.getNamespaceURI())) {
        rule(errors, "invalid", path, !empty(element), "Elements must have some content (@value, @id, extensions, or children elements)");
      }
      Map<String, ElementComponent> children = getChildren(structure, definition);
      ChildIterator ci = new ChildIterator(path, element);
      while (ci.next()) {
        ElementComponent child = children.get(ci.name());
        String type = null;
        if (ci.name().equals("extension")) {
          type = "Extension";
          child = definition; // it's going to be used as context below
        } else if (child == null) {
          child = getDefinitionByTailNameChoice(children, ci.name());
          if (child != null)
            type = ci.name().substring(tail(child.getPathSimple()).length() - 3);
          if ("Resource".equals(type))
            type = "ResourceReference";
        } else {
          if (child.getDefinition().getType().size() > 1)
            throw new Exception("multiple types?");
          if (child.getDefinition().getType().size() == 1)
            type = child.getDefinition().getType().get(0).getCodeSimple();
          if (type != null) {
            if (type.startsWith("Resource("))
              type = "ResourceReference";
            if (type.startsWith("@")) {
              child = findElement(structure, type.substring(1));
              type = null;
            }
          }       
        }
        if (type != null) {
          if (typeIsPrimitive(type)) 
            checkPrimitive(errors, ci.path(), type, child, ci.element());
          else {
            if (type.equals("Identifier"))
              checkIdentifier(ci.path(), ci.element(), child);
            else if (type.equals("Coding"))
              checkCoding(errors, ci.path(), ci.element(), profile, child);
            else if (type.equals("CodeableConcept"))
              checkCodeableConcept(errors, ci.path(), ci.element(), profile, child);
            if (type.equals("Resource"))
              validateContains(errors, ci.path(), child, definition, ci.element());
            else {
              Profile p = getProfileForType(type); 
              ProfileStructureComponent r = getStructureForType(p, type);
              if (rule(errors, "structure", ci.path(), r != null, "Unknown type "+type)) {
                validateElement(errors, p, r, ci.path(), r.getElement().get(0), profile, child, ci.element());
              }
            }
          }
        } else {
          if (rule(errors, "structure", path, child != null, "Unrecognised Content "+ci.name()))
            validateElement(errors, profile, structure, ci.path(), child, null, null, ci.element());
        }
      }
    }
  
    private boolean empty(Element element) {
      if (element.hasAttribute("value"))
        return false;
      if (element.hasAttribute("id"))
        return false;
      if (element.hasAttribute("xml:id"))
        return false;
      Element child = XMLUtil.getFirstChild(element);
      while (child != null) {
        if (NS_FHIR.equals(child.getNamespaceURI()))
          return false;        
      }
      return true;
    }

    private ElementComponent findElement(ProfileStructureComponent structure, String name) {
      for (ElementComponent c : structure.getElement()) {
        if (c.getPathSimple().equals(name)) {
          return c;
        }
      }
      return null;
    }
  
    private Map<String, ElementComponent> getChildren(ProfileStructureComponent structure, ElementComponent definition) {
      HashMap<String, ElementComponent> res = new HashMap<String, Profile.ElementComponent>(); 
      for (ElementComponent e : structure.getElement()) {
        if (e.getPathSimple().startsWith(definition.getPathSimple()+".") && !e.getPathSimple().equals(definition.getPathSimple())) {
          String tail = e.getPathSimple().substring(definition.getPathSimple().length()+1);
          if (!tail.contains(".")) {
            res.put(tail, e);
          }
        }
      }
      return res;
    }
  
    private ElementComponent getDefinitionByTailNameChoice(Map<String, ElementComponent> children, String name) {
      for (String n : children.keySet()) {
        if (n.endsWith("[x]") && name.startsWith(n.substring(0, n.length()-3))) {
          return children.get(n);
        }
      }
      return null;
    }
  
    private String tail(String path) {
      return path.substring(path.lastIndexOf(".")+1);
    }
  
    private void validateContains(List<ValidationMessage> errors, String path, ElementComponent child, ElementComponent context, Element element) throws Exception {
      Element e = XMLUtil.getFirstChild(element);
      validate(errors, path, e);    
    }
  
    private boolean typeIsPrimitive(String t) {
      if ("boolean".equalsIgnoreCase(t)) return true;
      if ("integer".equalsIgnoreCase(t)) return true;
      if ("decimal".equalsIgnoreCase(t)) return true;
      if ("base64Binary".equalsIgnoreCase(t)) return true;
      if ("instant".equalsIgnoreCase(t)) return true;
      if ("string".equalsIgnoreCase(t)) return true;
      if ("uri".equalsIgnoreCase(t)) return true;
      if ("date".equalsIgnoreCase(t)) return true;
      if ("date".equalsIgnoreCase(t)) return true;
      if ("dateTime".equalsIgnoreCase(t)) return true;
      if ("date".equalsIgnoreCase(t)) return true;
      if ("oid".equalsIgnoreCase(t)) return true;
      if ("uuid".equalsIgnoreCase(t)) return true;
      if ("code".equalsIgnoreCase(t)) return true;
      if ("id".equalsIgnoreCase(t)) return true;
      if ("xhtml".equalsIgnoreCase(t)) return true;
      return false;
    }
  
    private void checkPrimitive(List<ValidationMessage> errors, String path, String type, ElementComponent context, Element e) {
      if (type.equals("uri")) {
        rule(errors, "invalid", path, !e.getAttribute("value").startsWith("oid:"), "URI values cannot start with oid:");
        rule(errors, "invalid", path, !e.getAttribute("value").startsWith("uuid:"), "URI values cannot start with uuid:");
      }
        
      // for nothing to check    
    }
  
    private void checkExtension(String path, ElementComponent elementDefn, ElementComponent context, Element e) {
      // for now, nothing to check yet
      
    }
  
    private void checkResourceReference(String path, Element element, ElementComponent context, boolean b) {
      // nothing to do yet
      
    }
  
    private void checkIdentifier(String path, Element element, ElementComponent context) {
      
    }
  
    private void checkQuantity(List<ValidationMessage> errors, String path, Element element, ElementComponent context, boolean b) {
      String code = XMLUtil.getNamedChildValue(element,  "code");
      String system = XMLUtil.getNamedChildValue(element,  "system");
      String units = XMLUtil.getNamedChildValue(element,  "units");
      
      if (system != null && code != null) {
        checkCode(errors, path, code, system, units);
      }
      
    }
  
  
    private void checkCoding(List<ValidationMessage> errors, String path, Element element, Profile profile, ElementComponent context) {
      String code = XMLUtil.getNamedChildValue(element,  "code");
      String system = XMLUtil.getNamedChildValue(element,  "system");
      String display = XMLUtil.getNamedChildValue(element,  "display");
      
      if (system != null && code != null) {
        if (checkCode(errors, path, code, system, display)) 
            if (context != null && context.getDefinition().getBinding() != null) {
              ProfileBindingComponent binding = getBinding(profile, context.getDefinition().getBindingSimple());
              if (warning(errors, "code-unknown", path, binding != null, "Binding "+context.getDefinition().getBindingSimple()+" not resolved")) {
                if (binding.getTypeSimple() == BindingType.valueset) {
                  ValueSet vs = resolveBindingReference(binding.getReference());
                  if (warning(errors, "code-unknown", path, vs != null, "ValueSet "+describeReference(binding.getReference())+" not found")) {
                    try {
                      vs = cache.getExpander().expand(vs);
                      if (warning(errors, "code-unknown", path, vs != null, "Unable to expand value set for "+context.getDefinition().getBindingSimple())) {
                        warning(errors, "code-unknown", path, codeInExpansion(vs, system, code), "Code {"+system+"}"+code+" is not in value set "+context.getDefinition().getBindingSimple()+" ("+vs.getIdentifierSimple()+")");
                      }
                    } catch (Exception e) {
                      if (e.getMessage() == null)
                        warning(errors, "code-unknown", path, false, "Exception opening value set "+vs.getIdentifierSimple()+" for "+context.getDefinition().getBindingSimple()+": --Null--");
                      else if (!e.getMessage().contains("unable to find value set http://snomed.info"))
                        hint(errors, "code-unknown", path, suppressLoincSnomedMessages, "Snomed value set - not validated");
                      else if (!e.getMessage().contains("unable to find value set http://loinc.org"))
                        hint(errors, "code-unknown", path, suppressLoincSnomedMessages, "Loinc value set - not validated");
                      else
                        warning(errors, "code-unknown", path, false, "Exception opening value set "+vs.getIdentifierSimple()+" for "+context.getDefinition().getBindingSimple()+": "+e.getMessage());
                    }
                  }
                } else if (binding.getTypeSimple() == BindingType.codelist)
                  warning(errors, "code-unknown", path, false, "Binding type codelist should not be used with CodeableConcept");
                else if (binding.getTypeSimple() == BindingType.reference)
                  hint(errors, "code-unknown", path, false, "Binding type reference cannot be enforced by the validator");
                else if (binding.getTypeSimple() == BindingType.special)
                  warning(errors, "code-unknown", path, false, "Binding type codelist not implemented");
                //else if (binding.getTypeSimple() == BindingType.unbound)
                 // nothing
              }
            }
      }
    }
  

    private ValueSet resolveBindingReference(Type reference) {
      if (reference instanceof Uri)
        return valuesets.get(((Uri) reference).getValue().toString());
      else if (reference instanceof ResourceReference)
        return valuesets.get(((ResourceReference) reference).getReferenceSimple());
      else
        return null;
    }

    private boolean codeInExpansion(ValueSet vs, String system, String code) {
      for (ValueSetExpansionContainsComponent c : vs.getExpansion().getContains()) {
        if (code.equals(c.getCodeSimple()) && system.equals(c.getSystemSimple()))
          return true;
        if (codeinExpansion(c, system, code)) 
          return true;
      }
      return false;
    }

    private boolean codeinExpansion(ValueSetExpansionContainsComponent cnt, String system, String code) {
      for (ValueSetExpansionContainsComponent c : cnt.getContains()) {
        if (code.equals(c.getCodeSimple()) && system.equals(c.getSystemSimple().toString()))
          return true;
        if (codeinExpansion(c, system, code)) 
          return true;
      }
      return false;
    }

    private void checkCodeableConcept(List<ValidationMessage> errors, String path, Element element, Profile profile, ElementComponent context) {
      if (context != null && context.getDefinition().getBinding() != null) {
        ProfileBindingComponent binding = getBinding(profile, context.getDefinition().getBindingSimple());
        if (warning(errors, "code-unknown", path, binding != null, "Binding "+context.getDefinition().getBindingSimple()+" not resolved (cc)")) {
          if (binding.getTypeSimple() == BindingType.valueset) {
            ValueSet vs = resolveBindingReference(binding.getReference());
            if (warning(errors, "code-unknown", path, vs != null, "ValueSet "+describeReference(binding.getReference())+" not found")) {
              try {
                vs = cache.getExpander().expand(vs);
                if (warning(errors, "code-unknown", path, binding != null, "Unable to expand value set for "+context.getDefinition().getBindingSimple())) {
                  boolean found = false;
                  boolean any = false;
                  Element c = XMLUtil.getFirstChild(element);
                  while (c != null) {
                    if (c.getNodeName().equals("coding")) {
                      any = true;
                      String system = XMLUtil.getNamedChildValue(c, "system");
                      String code = XMLUtil.getNamedChildValue(c, "code");
                      if (system != null && code != null)
                        found = found || codeInExpansion(vs, system, code);
                    }
                    c = XMLUtil.getNextSibling(c);
                  }
                  if (!any && binding.getConformanceSimple() == BindingConformance.required)
                    warning(errors, "code-unknown", path, found, "No code provided, and value set "+context.getDefinition().getBindingSimple()+" ("+vs.getIdentifierSimple()+") is required");
                  if (any)
                    if (binding.getConformanceSimple() == BindingConformance.example)
                      hint(errors, "code-unknown", path, found, "None of the codes are in the example value set "+context.getDefinition().getBindingSimple()+" ("+vs.getIdentifierSimple()+")");
                    else 
                      warning(errors, "code-unknown", path, found, "None of the codes are in the expected value set "+context.getDefinition().getBindingSimple()+" ("+vs.getIdentifierSimple()+")");
                }
              } catch (Exception e) {
                if (e.getMessage() == null) {
                  warning(errors, "code-unknown", path, false, "Exception opening value set "+vs.getIdentifierSimple()+" for "+context.getDefinition().getBindingSimple()+": --Null--");
                } else if (!e.getMessage().contains("unable to find value set http://snomed.info")) {
                  hint(errors, "code-unknown", path, suppressLoincSnomedMessages, "Snomed value set - not validated");
                } else if (!e.getMessage().contains("unable to find value set http://loinc.org")) { 
                  hint(errors, "code-unknown", path, suppressLoincSnomedMessages, "Loinc value set - not validated");
                } else
                  warning(errors, "code-unknown", path, false, "Exception opening value set "+vs.getIdentifierSimple()+" for "+context.getDefinition().getBindingSimple()+": "+e.getMessage());
              }
            }
          } else if (binding.getTypeSimple() == BindingType.codelist)
            warning(errors, "code-unknown", path, false, "Binding type codelist should not be used with CodeableConcept");
          else if (binding.getTypeSimple() == BindingType.reference)
            hint(errors, "code-unknown", path, false, "Binding type reference cannot be enforced by the validator");
          else if (binding.getTypeSimple() == BindingType.special)
            warning(errors, "code-unknown", path, false, "Binding type codelist not implemented (cc)");
          //else if (binding.getTypeSimple() == BindingType.unbound)
           // nothing
        }
      }
    }
  
    private String describeReference(Type reference) {
      if (reference == null)
        return "null";
      if (reference instanceof Uri)
        return ((Uri)reference).getValue();
      if (reference instanceof ResourceReference)
        return ((ResourceReference)reference).getReference().getValue();
      return "??";
    }

    private ProfileBindingComponent getBinding(Profile profile, String name) {
      for (ProfileBindingComponent b : profile.getBinding()) {
        if (b.getNameSimple().equals(name))
          return b;
      }
      return null;
    }

    private boolean checkCode(List<ValidationMessage> errors, String path, String code, String system, String display) {
      if (system.startsWith("http://hl7.org/fhir")) {
        if (system.equals("http://hl7.org/fhir/sid/icd-10"))
            return true; // else don't check ICD-10 (for now)
          else {
            ValueSet vs = getValueSet(system);
            if (warning(errors, "code-unknown", path, vs != null, "Unknown Code System "+system)) {
              ValueSetDefineConceptComponent def = getCodeDefinition(vs, code); 
              if (warning(errors, "code-unknown", path, def != null, "Unknown Code ("+system+"#"+code+")"))
                return warning(errors, "code-unknown", path, display == null || display.equals(def.getDisplaySimple()), "Display should be '"+def.getDisplaySimple()+"'");
            }
            return false;
          }
      } else if (system.startsWith("http://loinc.org")) {
        return true;
      } else if (system.startsWith("http://unitsofmeasure.org")) {
        return true;
      }
      else 
        return true;
    }
  
    private ValueSetDefineConceptComponent getCodeDefinition(ValueSetDefineConceptComponent c, String code) {
      if (code.equals(c.getCodeSimple()))
        return c;
      for (ValueSetDefineConceptComponent g : c.getConcept()) {
        ValueSetDefineConceptComponent r = getCodeDefinition(g, code);
        if (r != null)
          return r;
      }
      return null;
    }
    
    private ValueSetDefineConceptComponent getCodeDefinition(ValueSet vs, String code) {
      for (ValueSetDefineConceptComponent c : vs.getDefine().getConcept()) {
        ValueSetDefineConceptComponent r = getCodeDefinition(c, code);
        if (r != null)
          return r;
      }
      return null;
    }
  
    private ValueSet getValueSet(String system) {
      return codesystems.get(system);
    }

    public boolean isSuppressLoincSnomedMessages() {
      return suppressLoincSnomedMessages;
    }

    public void setSuppressLoincSnomedMessages(boolean suppressLoincSnomedMessages) {
      this.suppressLoincSnomedMessages = suppressLoincSnomedMessages;
    }

    public void validateInstanceByProfile(List<ValidationMessage> errors, Element root, Profile profile) throws Exception {
      // we assume that the following things are true: 
      // the instance at root is valid against the schema and schematron
      // the instance validator had no issues against the base resource profile
      if (root.getLocalName().equals("feed")) {
        throw new Exception("not done yet");
      }
      else {
        // so the first question is what to validate against
        ProfileStructureComponent sc = null;
        for (ProfileStructureComponent s : profile.getStructure()) {
          if (root.getLocalName().equals(s.getTypeSimple())) {
            if (sc == null)
              sc = s;
            else
              throw new Exception("the profile contains multiple matches for the resource "+root.getLocalName()+" and the profile cannot be validated against");
          }
        }
        if (rule(errors, "invalid", root.getLocalName(), sc != null, "Profile does not allow for this resource")) {
          // well, does it conform to the resource?
          // this is different to the case above because there may be more than one option at each point, and we could conform to any one of them
          
          
        }
      }
    }
  }
  