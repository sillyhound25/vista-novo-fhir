﻿using System;
using System.Collections.Generic;
using Hl7.Fhir.Support;
using System.Xml.Linq;
using System.Linq;

/*
  Copyright (c) 2011-2013, HL7, Inc.
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

//
// Generated on Wed, Oct 2, 2013 07:37+1000 for FHIR v0.11
//

using Hl7.Fhir.Model;
using System.Xml;

namespace Hl7.Fhir.Parsers
{
    /// <summary>
    /// Parser for Encounter instances
    /// </summary>
    internal static partial class EncounterParser
    {
        /// <summary>
        /// Parse Encounter
        /// </summary>
        public static Hl7.Fhir.Model.Encounter ParseEncounter(IFhirReader reader, ErrorList errors, Hl7.Fhir.Model.Encounter existingInstance = null )
        {
            Hl7.Fhir.Model.Encounter result = existingInstance != null ? existingInstance : new Hl7.Fhir.Model.Encounter();
            string currentElementName = reader.CurrentElementName;
            reader.EnterElement();
            
            while (reader.HasMoreElements())
            {
                var atName = reader.CurrentElementName;
                // Parse element extension
                if( atName == "extension" )
                {
                    result.Extension = new List<Hl7.Fhir.Model.Extension>();
                    reader.EnterArray();
                    
                    while( ParserUtils.IsAtArrayElement(reader, "extension") )
                        result.Extension.Add(ExtensionParser.ParseExtension(reader, errors));
                    
                    reader.LeaveArray();
                }
                
                // Parse element language
                else if( atName == "language" )
                    result.LanguageElement = CodeParser.ParseCode(reader, errors);
                
                // Parse element text
                else if( atName == "text" )
                    result.Text = NarrativeParser.ParseNarrative(reader, errors);
                
                // Parse element contained
                else if( atName == "contained" )
                {
                    result.Contained = new List<Hl7.Fhir.Model.Resource>();
                    reader.EnterArray();
                    
                    while( ParserUtils.IsAtArrayElement(reader, "contained") )
                        result.Contained.Add(ParserUtils.ParseContainedResource(reader,errors));
                    
                    reader.LeaveArray();
                }
                
                // Parse element _id
                else if( atName == "_id" )
                    result.LocalIdElement = Id.Parse(reader.ReadPrimitiveContents(typeof(Id)));
                
                // Parse element identifier
                else if( atName == "identifier" )
                {
                    result.Identifier = new List<Hl7.Fhir.Model.Identifier>();
                    reader.EnterArray();
                    
                    while( ParserUtils.IsAtArrayElement(reader, "identifier") )
                        result.Identifier.Add(IdentifierParser.ParseIdentifier(reader, errors));
                    
                    reader.LeaveArray();
                }
                
                // Parse element status
                else if( atName == "status" )
                    result.StatusElement = CodeParser.ParseCode<Hl7.Fhir.Model.Encounter.EncounterState>(reader, errors);
                
                // Parse element class
                else if( atName == "class" )
                    result.ClassElement = CodeParser.ParseCode<Hl7.Fhir.Model.Encounter.EncounterClass>(reader, errors);
                
                // Parse element type
                else if( atName == "type" )
                {
                    result.Type = new List<Hl7.Fhir.Model.CodeableConcept>();
                    reader.EnterArray();
                    
                    while( ParserUtils.IsAtArrayElement(reader, "type") )
                        result.Type.Add(CodeableConceptParser.ParseCodeableConcept(reader, errors));
                    
                    reader.LeaveArray();
                }
                
                // Parse element subject
                else if( atName == "subject" )
                    result.Subject = ResourceReferenceParser.ParseResourceReference(reader, errors);
                
                // Parse element participant
                else if( atName == "participant" )
                {
                    result.Participant = new List<Hl7.Fhir.Model.Encounter.EncounterParticipantComponent>();
                    reader.EnterArray();
                    
                    while( ParserUtils.IsAtArrayElement(reader, "participant") )
                        result.Participant.Add(EncounterParser.ParseEncounterParticipantComponent(reader, errors));
                    
                    reader.LeaveArray();
                }
                
                // Parse element fulfills
                else if( atName == "fulfills" )
                    result.Fulfills = ResourceReferenceParser.ParseResourceReference(reader, errors);
                
                // Parse element start
                else if( atName == "start" )
                    result.StartElement = FhirDateTimeParser.ParseFhirDateTime(reader, errors);
                
                // Parse element length
                else if( atName == "length" )
                    result.Length = DurationParser.ParseDuration(reader, errors);
                
                // Parse element reason
                else if( atName.StartsWith("reason") )
                    result.Reason = FhirParser.ParseElement(reader, errors);
                
                // Parse element indication
                else if( atName == "indication" )
                    result.Indication = ResourceReferenceParser.ParseResourceReference(reader, errors);
                
                // Parse element priority
                else if( atName == "priority" )
                    result.Priority = CodeableConceptParser.ParseCodeableConcept(reader, errors);
                
                // Parse element hospitalization
                else if( atName == "hospitalization" )
                    result.Hospitalization = EncounterParser.ParseEncounterHospitalizationComponent(reader, errors);
                
                // Parse element location
                else if( atName == "location" )
                {
                    result.Location = new List<Hl7.Fhir.Model.Encounter.EncounterLocationComponent>();
                    reader.EnterArray();
                    
                    while( ParserUtils.IsAtArrayElement(reader, "location") )
                        result.Location.Add(EncounterParser.ParseEncounterLocationComponent(reader, errors));
                    
                    reader.LeaveArray();
                }
                
                // Parse element serviceProvider
                else if( atName == "serviceProvider" )
                    result.ServiceProvider = ResourceReferenceParser.ParseResourceReference(reader, errors);
                
                // Parse element partOf
                else if( atName == "partOf" )
                    result.PartOf = ResourceReferenceParser.ParseResourceReference(reader, errors);
                
                else
                {
                    errors.Add(String.Format("Encountered unknown element {0} while parsing {1}", reader.CurrentElementName, currentElementName), reader);
                    reader.SkipSubElementsFor(currentElementName);
                    result = null;
                }
            }
            
            reader.LeaveElement();
            return result;
        }
        
        /// <summary>
        /// Parse EncounterHospitalizationComponent
        /// </summary>
        public static Hl7.Fhir.Model.Encounter.EncounterHospitalizationComponent ParseEncounterHospitalizationComponent(IFhirReader reader, ErrorList errors, Hl7.Fhir.Model.Encounter.EncounterHospitalizationComponent existingInstance = null )
        {
            Hl7.Fhir.Model.Encounter.EncounterHospitalizationComponent result = existingInstance != null ? existingInstance : new Hl7.Fhir.Model.Encounter.EncounterHospitalizationComponent();
            string currentElementName = reader.CurrentElementName;
            reader.EnterElement();
            
            while (reader.HasMoreElements())
            {
                var atName = reader.CurrentElementName;
                // Parse element extension
                if( atName == "extension" )
                {
                    result.Extension = new List<Hl7.Fhir.Model.Extension>();
                    reader.EnterArray();
                    
                    while( ParserUtils.IsAtArrayElement(reader, "extension") )
                        result.Extension.Add(ExtensionParser.ParseExtension(reader, errors));
                    
                    reader.LeaveArray();
                }
                
                // Parse element _id
                else if( atName == "_id" )
                    result.LocalIdElement = Id.Parse(reader.ReadPrimitiveContents(typeof(Id)));
                
                // Parse element preAdmissionIdentifier
                else if( atName == "preAdmissionIdentifier" )
                    result.PreAdmissionIdentifier = IdentifierParser.ParseIdentifier(reader, errors);
                
                // Parse element origin
                else if( atName == "origin" )
                    result.Origin = ResourceReferenceParser.ParseResourceReference(reader, errors);
                
                // Parse element admitSource
                else if( atName == "admitSource" )
                    result.AdmitSource = CodeableConceptParser.ParseCodeableConcept(reader, errors);
                
                // Parse element period
                else if( atName == "period" )
                    result.Period = PeriodParser.ParsePeriod(reader, errors);
                
                // Parse element accomodation
                else if( atName == "accomodation" )
                {
                    result.Accomodation = new List<Hl7.Fhir.Model.Encounter.EncounterHospitalizationAccomodationComponent>();
                    reader.EnterArray();
                    
                    while( ParserUtils.IsAtArrayElement(reader, "accomodation") )
                        result.Accomodation.Add(EncounterParser.ParseEncounterHospitalizationAccomodationComponent(reader, errors));
                    
                    reader.LeaveArray();
                }
                
                // Parse element diet
                else if( atName == "diet" )
                    result.Diet = CodeableConceptParser.ParseCodeableConcept(reader, errors);
                
                // Parse element specialCourtesy
                else if( atName == "specialCourtesy" )
                {
                    result.SpecialCourtesy = new List<Hl7.Fhir.Model.CodeableConcept>();
                    reader.EnterArray();
                    
                    while( ParserUtils.IsAtArrayElement(reader, "specialCourtesy") )
                        result.SpecialCourtesy.Add(CodeableConceptParser.ParseCodeableConcept(reader, errors));
                    
                    reader.LeaveArray();
                }
                
                // Parse element specialArrangement
                else if( atName == "specialArrangement" )
                {
                    result.SpecialArrangement = new List<Hl7.Fhir.Model.CodeableConcept>();
                    reader.EnterArray();
                    
                    while( ParserUtils.IsAtArrayElement(reader, "specialArrangement") )
                        result.SpecialArrangement.Add(CodeableConceptParser.ParseCodeableConcept(reader, errors));
                    
                    reader.LeaveArray();
                }
                
                // Parse element destination
                else if( atName == "destination" )
                    result.Destination = ResourceReferenceParser.ParseResourceReference(reader, errors);
                
                // Parse element dischargeDisposition
                else if( atName == "dischargeDisposition" )
                    result.DischargeDisposition = CodeableConceptParser.ParseCodeableConcept(reader, errors);
                
                // Parse element reAdmission
                else if( atName == "reAdmission" )
                    result.ReAdmissionElement = FhirBooleanParser.ParseFhirBoolean(reader, errors);
                
                else
                {
                    errors.Add(String.Format("Encountered unknown element {0} while parsing {1}", reader.CurrentElementName, currentElementName), reader);
                    reader.SkipSubElementsFor(currentElementName);
                    result = null;
                }
            }
            
            reader.LeaveElement();
            return result;
        }
        
        /// <summary>
        /// Parse EncounterHospitalizationAccomodationComponent
        /// </summary>
        public static Hl7.Fhir.Model.Encounter.EncounterHospitalizationAccomodationComponent ParseEncounterHospitalizationAccomodationComponent(IFhirReader reader, ErrorList errors, Hl7.Fhir.Model.Encounter.EncounterHospitalizationAccomodationComponent existingInstance = null )
        {
            Hl7.Fhir.Model.Encounter.EncounterHospitalizationAccomodationComponent result = existingInstance != null ? existingInstance : new Hl7.Fhir.Model.Encounter.EncounterHospitalizationAccomodationComponent();
            string currentElementName = reader.CurrentElementName;
            reader.EnterElement();
            
            while (reader.HasMoreElements())
            {
                var atName = reader.CurrentElementName;
                // Parse element extension
                if( atName == "extension" )
                {
                    result.Extension = new List<Hl7.Fhir.Model.Extension>();
                    reader.EnterArray();
                    
                    while( ParserUtils.IsAtArrayElement(reader, "extension") )
                        result.Extension.Add(ExtensionParser.ParseExtension(reader, errors));
                    
                    reader.LeaveArray();
                }
                
                // Parse element _id
                else if( atName == "_id" )
                    result.LocalIdElement = Id.Parse(reader.ReadPrimitiveContents(typeof(Id)));
                
                // Parse element bed
                else if( atName == "bed" )
                    result.Bed = ResourceReferenceParser.ParseResourceReference(reader, errors);
                
                // Parse element period
                else if( atName == "period" )
                    result.Period = PeriodParser.ParsePeriod(reader, errors);
                
                else
                {
                    errors.Add(String.Format("Encountered unknown element {0} while parsing {1}", reader.CurrentElementName, currentElementName), reader);
                    reader.SkipSubElementsFor(currentElementName);
                    result = null;
                }
            }
            
            reader.LeaveElement();
            return result;
        }
        
        /// <summary>
        /// Parse EncounterLocationComponent
        /// </summary>
        public static Hl7.Fhir.Model.Encounter.EncounterLocationComponent ParseEncounterLocationComponent(IFhirReader reader, ErrorList errors, Hl7.Fhir.Model.Encounter.EncounterLocationComponent existingInstance = null )
        {
            Hl7.Fhir.Model.Encounter.EncounterLocationComponent result = existingInstance != null ? existingInstance : new Hl7.Fhir.Model.Encounter.EncounterLocationComponent();
            string currentElementName = reader.CurrentElementName;
            reader.EnterElement();
            
            while (reader.HasMoreElements())
            {
                var atName = reader.CurrentElementName;
                // Parse element extension
                if( atName == "extension" )
                {
                    result.Extension = new List<Hl7.Fhir.Model.Extension>();
                    reader.EnterArray();
                    
                    while( ParserUtils.IsAtArrayElement(reader, "extension") )
                        result.Extension.Add(ExtensionParser.ParseExtension(reader, errors));
                    
                    reader.LeaveArray();
                }
                
                // Parse element _id
                else if( atName == "_id" )
                    result.LocalIdElement = Id.Parse(reader.ReadPrimitiveContents(typeof(Id)));
                
                // Parse element location
                else if( atName == "location" )
                    result.Location = ResourceReferenceParser.ParseResourceReference(reader, errors);
                
                // Parse element period
                else if( atName == "period" )
                    result.Period = PeriodParser.ParsePeriod(reader, errors);
                
                else
                {
                    errors.Add(String.Format("Encountered unknown element {0} while parsing {1}", reader.CurrentElementName, currentElementName), reader);
                    reader.SkipSubElementsFor(currentElementName);
                    result = null;
                }
            }
            
            reader.LeaveElement();
            return result;
        }
        
        /// <summary>
        /// Parse EncounterParticipantComponent
        /// </summary>
        public static Hl7.Fhir.Model.Encounter.EncounterParticipantComponent ParseEncounterParticipantComponent(IFhirReader reader, ErrorList errors, Hl7.Fhir.Model.Encounter.EncounterParticipantComponent existingInstance = null )
        {
            Hl7.Fhir.Model.Encounter.EncounterParticipantComponent result = existingInstance != null ? existingInstance : new Hl7.Fhir.Model.Encounter.EncounterParticipantComponent();
            string currentElementName = reader.CurrentElementName;
            reader.EnterElement();
            
            while (reader.HasMoreElements())
            {
                var atName = reader.CurrentElementName;
                // Parse element extension
                if( atName == "extension" )
                {
                    result.Extension = new List<Hl7.Fhir.Model.Extension>();
                    reader.EnterArray();
                    
                    while( ParserUtils.IsAtArrayElement(reader, "extension") )
                        result.Extension.Add(ExtensionParser.ParseExtension(reader, errors));
                    
                    reader.LeaveArray();
                }
                
                // Parse element _id
                else if( atName == "_id" )
                    result.LocalIdElement = Id.Parse(reader.ReadPrimitiveContents(typeof(Id)));
                
                // Parse element type
                else if( atName == "type" )
                {
                    result.TypeElement = new List<Code<Hl7.Fhir.Model.Encounter.ParticipantType>>();
                    reader.EnterArray();
                    
                    while( ParserUtils.IsAtArrayElement(reader, "type") )
                        result.TypeElement.Add(CodeParser.ParseCode<Hl7.Fhir.Model.Encounter.ParticipantType>(reader, errors));
                    
                    reader.LeaveArray();
                }
                
                // Parse element practitioner
                else if( atName == "practitioner" )
                    result.Practitioner = ResourceReferenceParser.ParseResourceReference(reader, errors);
                
                else
                {
                    errors.Add(String.Format("Encountered unknown element {0} while parsing {1}", reader.CurrentElementName, currentElementName), reader);
                    reader.SkipSubElementsFor(currentElementName);
                    result = null;
                }
            }
            
            reader.LeaveElement();
            return result;
        }
        
    }
}
