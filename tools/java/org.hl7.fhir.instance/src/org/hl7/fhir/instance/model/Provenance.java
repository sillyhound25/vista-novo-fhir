package org.hl7.fhir.instance.model;

/*
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

// Generated on Thu, Nov 8, 2012 23:13+1100 for FHIR v0.06

import java.util.*;

/**
 * Provenance information associated with another resource that can be used to help determine it's reliability or trace where the information in it came from
 */
public class Provenance extends Resource {

    public class Activity extends Element {
        /**
         * The period during which the activity occurred
         */
        private Period period;

        /**
         * The instant of time at which the activity was recorded
         */
        private java.util.Calendar recorded;

        /**
         * The reason that the activity was taking place
         */
        private CodeableConcept reason;

        /**
         * Where the activity occurred, if relevant
         */
        private Location location;

        /**
         * Policy or plan the activity was defined by
         */
        private java.net.URI policy;

        public Period getPeriod() { 
          return this.period;
        }

        public void setPeriod(Period value) { 
          this.period = value;
        }

        public java.util.Calendar getRecorded() { 
          return this.recorded;
        }

        public void setRecorded(java.util.Calendar value) { 
          this.recorded = value;
        }

        public CodeableConcept getReason() { 
          return this.reason;
        }

        public void setReason(CodeableConcept value) { 
          this.reason = value;
        }

        public Location getLocation() { 
          return this.location;
        }

        public void setLocation(Location value) { 
          this.location = value;
        }

        public java.net.URI getPolicy() { 
          return this.policy;
        }

        public void setPolicy(java.net.URI value) { 
          this.policy = value;
        }

    }

    public class Location extends Element {
        /**
         * The type of location - a classification of the kind of location at which the activity took place
         */
        private CodeableConcept type;

        /**
         * An identifier for the location
         */
        private Identifier id;

        /**
         * Human readable description of location at which the activity occurred
         */
        private String description;

        /**
         * Geospatial coordinates of the location
         */
        private String coords;

        public CodeableConcept getType() { 
          return this.type;
        }

        public void setType(CodeableConcept value) { 
          this.type = value;
        }

        public Identifier getId() { 
          return this.id;
        }

        public void setId(Identifier value) { 
          this.id = value;
        }

        public String getDescription() { 
          return this.description;
        }

        public void setDescription(String value) { 
          this.description = value;
        }

        public String getCoords() { 
          return this.coords;
        }

        public void setCoords(String value) { 
          this.coords = value;
        }

    }

    public class Party extends Element {
        /**
         * The type of the participant
         */
        private Coding type;

        /**
         * identity of participant. May be a logical or physical uri, and maybe absolute or relative
         */
        private java.net.URI id;

        /**
         * Human readable description of the participant
         */
        private String description;

        /**
         * The role that the participant played
         */
        private Coding role;

        public Coding getType() { 
          return this.type;
        }

        public void setType(Coding value) { 
          this.type = value;
        }

        public java.net.URI getId() { 
          return this.id;
        }

        public void setId(java.net.URI value) { 
          this.id = value;
        }

        public String getDescription() { 
          return this.description;
        }

        public void setDescription(String value) { 
          this.description = value;
        }

        public Coding getRole() { 
          return this.role;
        }

        public void setRole(Coding value) { 
          this.role = value;
        }

    }

    /**
     * The resource that this provenance information pertains to
     */
    private ResourceReference target;

    /**
     * The activity that was being undertaken that led to the creation of the resource being referenced
     */
    private Activity activity;

    /**
     * An entity that is involved in the provenance of the target resource
     */
    private List<Party> party = new ArrayList<Party>();

    /**
     * A digital signature on the target resource. The signature should reference a participant by xml:id
     */
    private String signature;

    public ResourceReference getTarget() { 
      return this.target;
    }

    public void setTarget(ResourceReference value) { 
      this.target = value;
    }

    public Activity getActivity() { 
      return this.activity;
    }

    public void setActivity(Activity value) { 
      this.activity = value;
    }

    public List<Party> getParty() { 
      return this.party;
    }

    public String getSignature() { 
      return this.signature;
    }

    public void setSignature(String value) { 
      this.signature = value;
    }


}

