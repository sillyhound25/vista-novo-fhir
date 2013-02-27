﻿/*
  Copyright (c) 2011-2012, HL7, Inc.
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

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Hl7.Fhir;
using Hl7.Fhir.Model;
using Hl7.Fhir.Support;
using System.Net;
using Hl7.Fhir.Parsers;



namespace Hl7.Fhir.Client
{
    public class FhirClient
    {
        //TODO: _include
        //TODO: Binaries

        public Uri FhirEndpoint { get; private set; }


        public FhirClient(Uri endpoint)
        {
            FhirEndpoint = endpoint;
            DefaultFormatRequestMethod = FormatRequestMethod.XmlAccept;
        }


        /// <summary>
        /// Get a conformance statement for the system
        /// </summary>
        /// <param name="useOptionsVerb">If true, uses the Http OPTIONS verb to get the conformance, otherwise uses the /metadata endpoint</param>
        /// <returns>A Conformance resource, or null if the server did not return status 200</returns>
        public Conformance Conformance(bool useOptionsVerb = false)
        {
            string path = useOptionsVerb ? "" : "metadata";
            var req = createRequest(path, false);
            req.Method = useOptionsVerb ? "OPTIONS" : "GET";

            doRequest(req);

            if (LastResponseDetails.Result == HttpStatusCode.OK)
            {
                Resource result = parseResource();

                if (!(result is Conformance))
                    throw new InvalidOperationException(
                        String.Format("Received a resource of type {0}, expected a Conformance resource", result.GetType().Name));

                return (Conformance)result;
            }
            else
                return null;
        }


        /// <summary>
        /// Fetches the latest version of a resource
        /// </summary>
        /// <param name="type">The type of resource to fetch</param>
        /// <param name="id">The id of the Resource to fetch</param>
        /// <returns>The requested resource, or null if the server did not return status 200</returns>
        public Resource Read(ResourceType type, string id)
        {
            var req =
                  createRequest(ResourceLocation.BuildResourceLocation(type.ToString(), id), false);
            req.Method = "GET";
            
            doRequest(req);

            if (LastResponseDetails.Result == HttpStatusCode.OK)
                return parseResource();
            else
                return null;
        }

        
        /// <summary>
        /// Fetches a specific version of a resource
        /// </summary>
        /// <param name="type">The type of resource to fetch</param>
        /// <param name="id">The id of the resource to fetch</param>
        /// <param name="versionId">The version id of the resource to fetch</param>
        /// <returns></returns>
        public Resource VRead(ResourceType type, string id, string versionId)
        {
            Uri vReadLocation = ResourceLocation.BuildVersionedResourceLocation(type.ToString(), id, versionId);
            var req = createRequest(vReadLocation.ToString(), false);
            req.Method = "GET";

            doRequest(req);

            if (LastResponseDetails.Result == HttpStatusCode.OK)
                return parseResource();
            else
                return null;
        }


        /// <summary>
        /// Update (or create) a resource
        /// </summary>
        /// <param name="resource"></param>
        /// <param name="id"></param>
        /// <returns></returns>
        public BundleEntry Update(BundleEntry resource, string id = null)
        {
            throw new NotImplementedException();
        }

        /// <summary>
        /// Delete a resource
        /// </summary>
        /// <param name="type"></param>
        /// <param name="id"></param>
        public void Delete(ResourceType type, string id)
        {
            throw new NotImplementedException();
        }


        /// <summary>
        /// Create a resource
        /// </summary>
        /// <param name="resource"></param>
        /// <returns></returns>
        public BundleEntry Create(BundleEntry resource)
        {
               throw new NotImplementedException();
        }



        /// <summary>
        /// Retrieve the version history for a specific resource instance
        /// </summary>
        /// <param name="type"></param>
        /// <param name="id"></param>
        /// <param name="lastUpdate"></param>
        /// <returns></returns>
	    public Bundle History(Resource type, string id, DateTimeOffset? lastUpdate = null )
        {
               throw new NotImplementedException();
        }

        /// <summary>
        /// Retrieve the version history for all resources of a certain type
        /// </summary>
        /// <param name="type"></param>
        /// <param name="lastUpdate"></param>
        /// <returns></returns>
        public Bundle History(ResourceType type, DateTimeOffset? lastUpdate = null )
        {
               throw new NotImplementedException();
        }

        /// <summary>
        /// Retrieve the version history of any resource on the server
        /// </summary>
        /// <param name="lastUpdate"></param>
        /// <returns></returns>
        public Bundle History(DateTimeOffset? lastUpdate = null)
        {
               throw new NotImplementedException();
        }


        /// <summary>
        /// Validates whether the contents of the resource would be acceptable as an update
        /// </summary>
        /// <param name="lastUpdate"></param>
        /// <returns></returns>
        public IssueReport Validate(string id, BundleEntry resource)
        {
            // Shoudn't this be a PUT instead of a POST?
            throw new NotImplementedException();
        }

        /// <summary>
        /// Search for resources based on search criteria
        /// </summary>
        /// <param name="type"></param>
        /// <param name="parameters"></param>
        /// <returns></returns>
        public Bundle Search(ResourceType type, Dictionary<string,string> parameters)
        {
               throw new NotImplementedException();
        }

	   
        /// <summary>
        /// Send a batched update to the server
        /// </summary>
        /// <param name="lastUpdate"></param>
        /// <returns></returns>
        public Bundle Batch(Bundle batch)
        {
               throw new NotImplementedException();
        }


        public FormatRequestMethod DefaultFormatRequestMethod { get; set; }

        /// <summary>
        /// The method used to communicate the preferred resource format
        /// </summary>
        public enum FormatRequestMethod
        {
            /// <summary>
            /// Use Accept header to request Xml
            /// </summary>
            XmlAccept,

            /// <summary>
            /// Use Accept header to request Json
            /// </summary>
            JsonAccept,

            /// <summary>
            /// Use the _format parameter to request Xml
            /// </summary>
            XmlFormatParam,

            /// <summary>
            /// Use the _format parameter to request Json
            /// </summary>
            JsonFormatParam
        }


        private HttpWebRequest createRequest(Uri path, bool forBundle)
        {
            return createRequest(path.ToString(), forBundle);
        }

        private HttpWebRequest createRequest(string path, bool forBundle)
        {
            Uri endpoint = new Uri( Util.Combine(FhirEndpoint.ToString(), path) );

            if( DefaultFormatRequestMethod == FormatRequestMethod.JsonFormatParam )
                endpoint = addParam(endpoint, ContentType.FORMAT_PARAM, ContentType.FORMAT_PARAM_JSON);
            if (DefaultFormatRequestMethod == FormatRequestMethod.XmlFormatParam)
                endpoint = addParam(endpoint, ContentType.FORMAT_PARAM, ContentType.FORMAT_PARAM_XML);

            var req = (HttpWebRequest)HttpWebRequest.Create(endpoint);
            req.UserAgent = "FhirClient for FHIR " + Model.ModelInfo.Version;

            if (DefaultFormatRequestMethod == FormatRequestMethod.XmlAccept)
                req.Accept = ContentType.BuildContentType(ContentType.ResourceFormat.Xml, forBundle);
            if (DefaultFormatRequestMethod == FormatRequestMethod.JsonAccept)
                req.Accept = ContentType.BuildContentType(ContentType.ResourceFormat.Json, forBundle);

            return req;
        }

        private static Uri addParam(Uri original, string paramName, string paramValue)
        {
            string url = original.GetLeftPart(UriPartial.Path);

            url += String.IsNullOrEmpty(original.Query) ? "?" : original.Query + "&";
            url += paramName + "=" + paramValue;

            return new Uri(url);
        }


        public ResponseDetails LastResponseDetails { get; private set; }

        private void doRequest(HttpWebRequest req)
        {
            HttpWebResponse response = (HttpWebResponse)req.GetResponseNoEx();

            try
            {
                LastResponseDetails = ResponseDetails.FromHttpWebResponse(response);
            }
            finally
            {
                response.Close();
            }
        }

        private Resource parseResource()
        {
            string data = LastResponseDetails.BodyAsString();
            string contentType = LastResponseDetails.ContentType;

            ErrorList parseErrors = new ErrorList();
            Resource result;

            ContentType.ResourceFormat format = ContentType.GetResourceFormatFromContentType(contentType);

            switch (format)
            {
                case ContentType.ResourceFormat.Json:
                    result = FhirParser.ParseResourceFromJson(data, parseErrors);
                    break;
                case ContentType.ResourceFormat.Xml:
                    result = FhirParser.ParseResourceFromXml(data, parseErrors);
                    break;
                default:
                    throw new FhirParseException("Cannot decode resource: unrecognized content type");
            }

            if (parseErrors.Count() > 0)
                throw new FhirParseException(data, parseErrors,
                    "Failed to parse the resource data: " + parseErrors.ToString());

            return result;
        }
    }
}
