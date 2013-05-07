package org.hl7.fhir.definitions.validation;

/*
 Copyright (c) 2011-2013, HL7, Inc
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
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.definitions.ecore.fhir.SearchParameter;
import org.hl7.fhir.definitions.model.BindingSpecification;
import org.hl7.fhir.definitions.model.BindingSpecification.BindingExtensibility;
import org.hl7.fhir.definitions.model.Definitions;
import org.hl7.fhir.definitions.model.ElementDefn;
import org.hl7.fhir.definitions.model.ResourceDefn;
import org.hl7.fhir.definitions.model.SearchParameter.SearchType;
import org.hl7.fhir.definitions.model.TypeRef;
import org.hl7.fhir.instance.model.Profile.SearchParamType;
import org.hl7.fhir.utilities.Utilities;


/** todo
 * Search parameters cannot include "-"
 * can't have element names in a resource that are part of resource itself
 * can't have an element name called id
 * can't have a search parameter called id
 * can't have an element name "entries"
 * banned parameter type names: history, metadata, mailbox, validation 
 * 
 * @author Grahame
 *
 */
public class ModelValidator {

  public enum Level {
    Hint,
    Warning,
    Error
  }

  private Definitions definitions;
	private List<ValidationMessage> errors = new ArrayList<ValidationMessage>();

	public ModelValidator(Definitions definitions) {
		super();
		this.definitions = definitions;
	}

	// public void setConceptDomains(List<ConceptDomain> conceptDomains) {
	// this.conceptDomains = conceptDomains;
	// }
	//
	// public void defineResource(String name) {
	// this.resources.add(name);
	// }
	//
	// public void setDataTypes(String[] names) throws Exception {
	// TypeParser tp = new TypeParser();
	// for (String tn : names) {
	// datatypes.addAll(tp.parse(tn));
	// }
	// }

  public List<ValidationMessage> checkStucture(String name, ElementDefn structure) {
    errors.clear();
    rule(structure.getName(), name.toLowerCase().substring(0, 1) != name.substring(0, 1), "Resource Name must start with an uppercase alpha character");
    checkElement(structure.getName(), structure, null, null);
    return errors;
  
  }
  
  public List<ValidationMessage> check(String name, ResourceDefn parent) {
		errors.clear();
    rule(parent.getName(), !name.equals("Metadata"), "The name 'Metadata' is not a legal name for a resource");
    rule(parent.getName(), !name.equals("History"), "The name 'History' is not a legal name for a resource");
    rule(parent.getName(), name.toLowerCase().substring(0, 1) != name.substring(0, 1), "Resource Name must start with an uppercase alpha character");

    checkElement(parent.getName(), parent.getRoot(), parent, null);
    rule(parent.getName(), parent.getRoot().getElementByName("text") == null, "Element named \"text\" not allowed");
    rule(parent.getName(), parent.getRoot().getElementByName("contained") == null, "Element named \"contaned\" not allowed");
    if (parent.getRoot().getElementByName("subject") != null && parent.getRoot().getElementByName("subject").typeCode().startsWith("Resource"))
      rule(parent.getName(), parent.getSearchParams().containsKey("subject"), "A resource that contains a subject reference must have a search parameter 'subject'");
    for (org.hl7.fhir.definitions.model.SearchParameter p : parent.getSearchParams().values()) {
      warning(parent.getName(), !p.getCode().contains("."), "Search Parameter Names cannot contain a '.' (\""+p.getCode()+"\")");
    }
//    rule(parent.getName(), !parent.getSearchParams().containsKey("id"), "A resource cannot have a search parameter 'id'");
		return errors;
	}

	//todo: check that primitives *in datatypes* don't repeat
	
	private void checkElement(String path, ElementDefn e, ResourceDefn parent, String parentName) {
		rule(path, e.unbounded() || e.getMaxCardinality() == 1,	"Max Cardinality must be 1 or unbounded");
		rule(path, e.getMinCardinality() == 0 || e.getMinCardinality() == 1, "Min Cardinality must be 0 or 1");
		hint(path, !nameOverlaps(e.getName(), parentName), "Name of child ("+e.getName()+") overlaps with name of parent ("+parentName+")");
		rule(path, e.hasShortDefn(), "Must have a short defn");
    warning(path, !Utilities.isPlural(e.getName()) || !e.unbounded(), "Element names should be singular");
    warning(path, !e.getName().equals("id"), "Element named \"id\" not allowed");
    rule(path, !e.getName().equals("extension"), "Element named \"extension\" not allowed");
    rule(path, !e.getName().equals("entries"), "Element named \"entries\" not allowed");
    rule(path, (parentName == null) || e.getName().charAt(0) == e.getName().toLowerCase().charAt(0), "Element Names must not start with an uppercase character");
    
// this isn't a real hint, just a way to gather information   hint(path, !e.isModifier(), "isModifier, minimum cardinality = "+e.getMinCardinality().toString());
    
    if( e.getShortDefn().length() > 0)
		{
			rule(path, e.getShortDefn().contains("|") || Character.isUpperCase(e.getShortDefn().charAt(0)) || !Character.isLetter(e.getShortDefn().charAt(0)), "Short Description must start with an uppercase character ('"+e.getShortDefn()+"')");
		    rule(path, !e.getShortDefn().endsWith(".") || e.getShortDefn().endsWith("etc."), "Short Description must not end with a period ('"+e.getShortDefn()+"')");
		    rule(path, e.getDefinition().contains("|") || Character.isUpperCase(e.getDefinition().charAt(0)) || !Character.isLetter(e.getDefinition().charAt(0)), "Long Description must start with an uppercase character ('"+e.getDefinition()+"')");
		}
		
    rule(path, !e.getName().startsWith("_"), "Element names cannot start with '_'");
		// if (e.getConformance() == ElementDefn.Conformance.Mandatory &&
		// !e.unbounded())
		// rule(path, e.getMinCardinality() > 0,
		// "Min Cardinality cannot be 0 when element is mandatory");
		//TODO: Really? A composite element need not have a definition?
		checkType(path, e, parent);
//		rule(path, !"code".equals(e.typeCode()) || e.hasBinding(),
//				"Must have a binding if type is 'code'");

		if (e.typeCode().equals("code") && parent != null) {
		  rule(path, e.hasBindingOrOk(), "An element of type code must have a binding");
		}
		
		if (e.hasBinding()) {
		  rule(path, e.typeCode().contains("code") || e.typeCode().contains("Coding") 
				  || e.typeCode().contains("CodeableConcept"), "Can only specify bindings for coded data types");
			BindingSpecification cd = definitions.getBindingByName(e.getBindingName());
			rule(path, cd != null, "Unable to resolve binding name " + e.getBindingName());
			
			if (cd != null) 
			  rule(path, (cd.getExtensibility() != BindingExtensibility.Extensible || (e.hasType("Coding") || e.hasType("CodeableConcept"))),
			        "A binding can only be extensible if an element has a type of Coding|CodeableConcept and the concept domain is bound directly to a code list.");
		}
		for (ElementDefn c : e.getElements()) {
			checkElement(path + "." + c.getName(), c, parent, e.getName());
		}

	}

  private boolean nameOverlaps(String name, String parentName) {
	  if (Utilities.noString(parentName))
	    return false;
	  name = name.toLowerCase();
	  parentName = parentName.toLowerCase();
	  if (parentName.startsWith(name))
	    return true;
	  for (int i = 3; i < name.length(); i++)
	    if (parentName.endsWith(name.substring(0, i)))
	      return true;
	  return false;
  }

  private void checkType(String path, ElementDefn e, ResourceDefn parent) {
		if (e.getTypes().size() == 0) {
			rule(path, path.contains("."), "Must have a type on a base element");
			rule(path, e.getName().equals("extension") || e.getElements().size() > 0, "Must have a type unless sub-elements exist");
		} else if (definitions.dataTypeIsSharedInfo(e.typeCode())) {
		  try {
        e.getElements().addAll(definitions.getElementDefn(e.typeCode()).getElements());
      } catch (Exception e1) {
        rule(path, false, e1.getMessage());
      }
		} else {
			for (TypeRef t : e.getTypes()) 
			{
				String s = t.getName();
				if (s.charAt(0) == '@') {
					//TODO: validate path
				} 
				else 
				{
					if (s.charAt(0) == '#')
						s = s.substring(1);
					if (!t.isSpecialType()) {
						rule(path, typeExists(s, parent), "Illegal Type '" + s + "'");
						if (t.isResourceReference()) {
							for (String p : t.getParams()) {
								rule(path,
										p.equals("Any")
												|| definitions.hasResource(p),
										"Unknown resource type " + p);
							}
						}
					}
				}
			}
		}

	}

	private boolean typeExists(String name, ResourceDefn parent) {
		return definitions.hasType(name) ||
				(parent != null && parent.getRoot().hasNestedType(name));
	}

	private boolean rule(String path, boolean b, String msg) {
		if (!b)
			errors.add(new ValidationMessage(path + ": " + msg, Level.Error));
		return b;

	}
  private boolean hint(String path, boolean b, String msg) {
    if (!b)
      errors.add(new ValidationMessage(path + ": " + msg, Level.Hint));
    return b;
    
  }

  private boolean warning(String path, boolean b, String msg) {
    if (!b)
      errors.add(new ValidationMessage(path + ": " + msg, Level.Warning));
    return b;
    
  }

}
