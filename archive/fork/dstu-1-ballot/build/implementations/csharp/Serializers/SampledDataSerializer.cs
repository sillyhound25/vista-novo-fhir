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
// Generated on Mon, Sep 30, 2013 03:15+1000 for FHIR v0.11
//

using Hl7.Fhir.Model;
using System.Xml;
using Newtonsoft.Json;
using Hl7.Fhir.Serializers;

namespace Hl7.Fhir.Serializers
{
    /*
    * Serializer for SampledData instances
    */
    internal static partial class SampledDataSerializer
    {
        public static void SerializeSampledData(Hl7.Fhir.Model.SampledData value, IFhirWriter writer, bool summary)
        {
            writer.WriteStartComplexContent();
            
            // Serialize element _id
            if(value.LocalIdElement != null)
                writer.WritePrimitiveContents("_id", value.LocalIdElement, XmlSerializationHint.Attribute);
            
            // Serialize element extension
            if(value.Extension != null && !summary && value.Extension.Count > 0)
            {
                writer.WriteStartArrayElement("extension");
                foreach(var item in value.Extension)
                {
                    writer.WriteStartArrayMember("extension");
                    ExtensionSerializer.SerializeExtension(item, writer, summary);
                    writer.WriteEndArrayMember();
                }
                writer.WriteEndArrayElement();
            }
            
            // Serialize element origin
            if(value.Origin != null)
            {
                writer.WriteStartElement("origin");
                QuantitySerializer.SerializeQuantity(value.Origin, writer, summary);
                writer.WriteEndElement();
            }
            
            // Serialize element period
            if(value.PeriodElement != null)
            {
                writer.WriteStartElement("period");
                FhirDecimalSerializer.SerializeFhirDecimal(value.PeriodElement, writer, summary);
                writer.WriteEndElement();
            }
            
            // Serialize element factor
            if(value.FactorElement != null)
            {
                writer.WriteStartElement("factor");
                FhirDecimalSerializer.SerializeFhirDecimal(value.FactorElement, writer, summary);
                writer.WriteEndElement();
            }
            
            // Serialize element lowerLimit
            if(value.LowerLimitElement != null)
            {
                writer.WriteStartElement("lowerLimit");
                FhirDecimalSerializer.SerializeFhirDecimal(value.LowerLimitElement, writer, summary);
                writer.WriteEndElement();
            }
            
            // Serialize element upperLimit
            if(value.UpperLimitElement != null)
            {
                writer.WriteStartElement("upperLimit");
                FhirDecimalSerializer.SerializeFhirDecimal(value.UpperLimitElement, writer, summary);
                writer.WriteEndElement();
            }
            
            // Serialize element dimensions
            if(value.DimensionsElement != null)
            {
                writer.WriteStartElement("dimensions");
                IntegerSerializer.SerializeInteger(value.DimensionsElement, writer, summary);
                writer.WriteEndElement();
            }
            
            // Serialize element data
            if(value.DataElement != null)
            {
                writer.WriteStartElement("data");
                FhirStringSerializer.SerializeFhirString(value.DataElement, writer, summary);
                writer.WriteEndElement();
            }
            
            
            writer.WriteEndComplexContent();
        }
        
    }
}