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

// Generated on Wed, Jan 23, 2013 13:24+1100 for FHIR v0.07

import java.util.*;

/**
 * Describes the intention of how one or more providers intend to deliver care for a particular patient for a period of time, possibly limited to care for a specific condition or set of conditions.
 */
public class CarePlan extends Resource {

    public enum CarePlanStatus {
        planned, // The plan is in development or awaiting use but is not yet intended to be acted upon.
        active, // The plan is intended to be followed and used as part of patient care
        ended, // The plan is no longer in use and is not expected to be followed or used in patient care
        Null; // added to help the parsers
        public static CarePlanStatus fromCode(String codeString) throws Exception {
            if (codeString == null || "".equals(codeString))
                return null;
        if ("planned".equals(codeString))
          return planned;
        if ("active".equals(codeString))
          return active;
        if ("ended".equals(codeString))
          return ended;
        throw new Exception("Unknown CarePlanStatus code '"+codeString+"'");
        }
        public String toCode() {
          switch (this) {
            case planned: return "planned";
            case active: return "active";
            case ended: return "ended";
            default: return "?";
          }
        }
    }

  public class CarePlanStatusEnumFactory implements EnumFactory {
    public Enum<?> fromCode(String codeString) throws Exception {
      if (codeString == null || "".equals(codeString))
            if (codeString == null || "".equals(codeString))
                return null;
        if ("planned".equals(codeString))
          return CarePlanStatus.planned;
        if ("active".equals(codeString))
          return CarePlanStatus.active;
        if ("ended".equals(codeString))
          return CarePlanStatus.ended;
        throw new Exception("Unknown CarePlanStatus code '"+codeString+"'");
        }
    public String toCode(Enum<?> code) throws Exception {
      if (code == CarePlanStatus.planned)
        return "planned";
      if (code == CarePlanStatus.active)
        return "active";
      if (code == CarePlanStatus.ended)
        return "ended";
      return "?";
      }
    }

    public enum CarePlanActivityCategory {
        diet, // Plan for the patient to consume food of a specified nature
        drug, // Plan for the patient to consume/receive a drug, vaccine or other product
        encounter, // Plan to meet or communicate with the patient (in-patient, out-patient, phone call, etc.)
        observation, // Plan to capture information about a patient (vitals, labs, diagnostic images, etc.)
        procedure, // Plan to modify the patient in some way (surgery, physio-therapy, education, counselling, etc.)
        supply, // Plan to provide something to the patient (medication, medical supply, etc.)
        other, // Some other form of action
        Null; // added to help the parsers
        public static CarePlanActivityCategory fromCode(String codeString) throws Exception {
            if (codeString == null || "".equals(codeString))
                return null;
        if ("diet".equals(codeString))
          return diet;
        if ("drug".equals(codeString))
          return drug;
        if ("encounter".equals(codeString))
          return encounter;
        if ("observation".equals(codeString))
          return observation;
        if ("procedure".equals(codeString))
          return procedure;
        if ("supply".equals(codeString))
          return supply;
        if ("other".equals(codeString))
          return other;
        throw new Exception("Unknown CarePlanActivityCategory code '"+codeString+"'");
        }
        public String toCode() {
          switch (this) {
            case diet: return "diet";
            case drug: return "drug";
            case encounter: return "encounter";
            case observation: return "observation";
            case procedure: return "procedure";
            case supply: return "supply";
            case other: return "other";
            default: return "?";
          }
        }
    }

  public class CarePlanActivityCategoryEnumFactory implements EnumFactory {
    public Enum<?> fromCode(String codeString) throws Exception {
      if (codeString == null || "".equals(codeString))
            if (codeString == null || "".equals(codeString))
                return null;
        if ("diet".equals(codeString))
          return CarePlanActivityCategory.diet;
        if ("drug".equals(codeString))
          return CarePlanActivityCategory.drug;
        if ("encounter".equals(codeString))
          return CarePlanActivityCategory.encounter;
        if ("observation".equals(codeString))
          return CarePlanActivityCategory.observation;
        if ("procedure".equals(codeString))
          return CarePlanActivityCategory.procedure;
        if ("supply".equals(codeString))
          return CarePlanActivityCategory.supply;
        if ("other".equals(codeString))
          return CarePlanActivityCategory.other;
        throw new Exception("Unknown CarePlanActivityCategory code '"+codeString+"'");
        }
    public String toCode(Enum<?> code) throws Exception {
      if (code == CarePlanActivityCategory.diet)
        return "diet";
      if (code == CarePlanActivityCategory.drug)
        return "drug";
      if (code == CarePlanActivityCategory.encounter)
        return "encounter";
      if (code == CarePlanActivityCategory.observation)
        return "observation";
      if (code == CarePlanActivityCategory.procedure)
        return "procedure";
      if (code == CarePlanActivityCategory.supply)
        return "supply";
      if (code == CarePlanActivityCategory.other)
        return "other";
      return "?";
      }
    }

    public class Participant extends Element {
        /**
         * Indicates specific responsibility of an individual within the care plan.  E.g. "Primary physician", "Team coordinator", "Caregiver", etc.
         */
        private CodeableConcept role;

        /**
         * The specific person or organization who is participating/expected to participate in the care plan.
         */
        private ResourceReference member;

        public CodeableConcept getRole() { 
          return this.role;
        }

        public void setRole(CodeableConcept value) { 
          this.role = value;
        }

        public ResourceReference getMember() { 
          return this.member;
        }

        public void setMember(ResourceReference value) { 
          this.member = value;
        }

  }

    public class Activity extends Element {
        /**
         * High-level categorization of the type of activity in a care plan.
         */
        private Enumeration<CarePlanActivityCategory> category;

        /**
         * Detailed description of the type of activity.  E.g. What lab test, what procedure, what kind of encounter.
         */
        private CodeableConcept code;

        /**
         * The timing or frequency upon which the described activity is to occur.
         */
        private Schedule schedule;

        /**
         * Identifies the facility where the activity will occur.  E.g. home, hospital, specific clinic, etc.
         */
        private ResourceReference location;

        /**
         * Identifies who's expected to be involved in the activity.
         */
        private List<ResourceReference> performer = new ArrayList<ResourceReference>();

        /**
         * Identifies the food, drug or other product being consumed or supplied in the activity.
         */
        private ResourceReference product;

        /**
         * Identifies the quantity expected to be consumed in a given day.
         */
        private Quantity dailyAmount;

        /**
         * Identifies the quantity expected to be supplied.
         */
        private Quantity quantity;

        /**
         * This provides a textual description of constraints on the activity occurrence, including relation to other activities.  It may also include objectives, pre-conditions and end-conditions.  Finally, it may convey specifics about the activity such as body site, method, route, etc.
         */
        private String_ details;

        /**
         * Resources resulting from the plan, such as drug prescriptions, encounter records, appointments, etc.
         */
        private List<ResourceReference> action = new ArrayList<ResourceReference>();

        public Enumeration<CarePlanActivityCategory> getCategory() { 
          return this.category;
        }

        public void setCategory(Enumeration<CarePlanActivityCategory> value) { 
          this.category = value;
        }

        public CarePlanActivityCategory getCategorySimple() { 
          return this.category.getValue();
        }

        public void setCategorySimple(CarePlanActivityCategory value) { 
          if (value == null)
            this.category = null;
          else {
            if (this.category == null)
              this.category = new Enumeration<CarePlanActivityCategory>();
            this.category.setValue(value);
          }
        }

        public CodeableConcept getCode() { 
          return this.code;
        }

        public void setCode(CodeableConcept value) { 
          this.code = value;
        }

        public Schedule getSchedule() { 
          return this.schedule;
        }

        public void setSchedule(Schedule value) { 
          this.schedule = value;
        }

        public ResourceReference getLocation() { 
          return this.location;
        }

        public void setLocation(ResourceReference value) { 
          this.location = value;
        }

        public List<ResourceReference> getPerformer() { 
          return this.performer;
        }

        public ResourceReference getProduct() { 
          return this.product;
        }

        public void setProduct(ResourceReference value) { 
          this.product = value;
        }

        public Quantity getDailyAmount() { 
          return this.dailyAmount;
        }

        public void setDailyAmount(Quantity value) { 
          this.dailyAmount = value;
        }

        public Quantity getQuantity() { 
          return this.quantity;
        }

        public void setQuantity(Quantity value) { 
          this.quantity = value;
        }

        public String_ getDetails() { 
          return this.details;
        }

        public void setDetails(String_ value) { 
          this.details = value;
        }

        public String getDetailsSimple() { 
          return this.details.getValue();
        }

        public void setDetailsSimple(String value) { 
          if (value == null)
            this.details = null;
          else {
            if (this.details == null)
              this.details = new String_();
            this.details.setValue(value);
          }
        }

        public List<ResourceReference> getAction() { 
          return this.action;
        }

  }

    /**
     * Identifies the patient/subject whose intended care is described by the plan.
     */
    private ResourceReference patient;

    /**
     * Indicates whether the plan is currently being acted upon, represents future intentions or is now just historical record.
     */
    private Enumeration<CarePlanStatus> status;

    /**
     * Indicates when the plan did (or is intended to) come into effect and end. 
     */
    private Period period;

    /**
     * Identifies the most recent date on which the plan has been revised.
     */
    private DateTime modified;

    /**
     * Identifies the problems/concerns/diagnoses/etc. whose management and/or mitigation are handled by this plan.
     */
    private List<ResourceReference> condition = new ArrayList<ResourceReference>();

    /**
     * Identifies all people and organizations who are expected to be involved in the care envisioned by this plan.
     */
    private List<Participant> participant = new ArrayList<Participant>();

    /**
     * Describes the intended objective(s) of carrying out the Care Plan.
     */
    private String_ goal;

    /**
     * Identifies a planned action to occur as part of the plan.  For example, a medication to be used, lab tests to perform, self-monitoring, education, etc.
     */
    private List<Activity> activity = new ArrayList<Activity>();

    public ResourceReference getPatient() { 
      return this.patient;
    }

    public void setPatient(ResourceReference value) { 
      this.patient = value;
    }

    public Enumeration<CarePlanStatus> getStatus() { 
      return this.status;
    }

    public void setStatus(Enumeration<CarePlanStatus> value) { 
      this.status = value;
    }

    public CarePlanStatus getStatusSimple() { 
      return this.status.getValue();
    }

    public void setStatusSimple(CarePlanStatus value) { 
      if (value == null)
        this.status = null;
      else {
        if (this.status == null)
          this.status = new Enumeration<CarePlanStatus>();
        this.status.setValue(value);
      }
    }

    public Period getPeriod() { 
      return this.period;
    }

    public void setPeriod(Period value) { 
      this.period = value;
    }

    public DateTime getModified() { 
      return this.modified;
    }

    public void setModified(DateTime value) { 
      this.modified = value;
    }

    public String getModifiedSimple() { 
      return this.modified.getValue();
    }

    public void setModifiedSimple(String value) { 
      if (value == null)
        this.modified = null;
      else {
        if (this.modified == null)
          this.modified = new DateTime();
        this.modified.setValue(value);
      }
    }

    public List<ResourceReference> getCondition() { 
      return this.condition;
    }

    public List<Participant> getParticipant() { 
      return this.participant;
    }

    public String_ getGoal() { 
      return this.goal;
    }

    public void setGoal(String_ value) { 
      this.goal = value;
    }

    public String getGoalSimple() { 
      return this.goal.getValue();
    }

    public void setGoalSimple(String value) { 
      if (value == null)
        this.goal = null;
      else {
        if (this.goal == null)
          this.goal = new String_();
        this.goal.setValue(value);
      }
    }

    public List<Activity> getActivity() { 
      return this.activity;
    }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.CarePlan;
   }


}
