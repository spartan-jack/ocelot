package com.vistatec.ocelot.config.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName(value="userProvenance")
public class ProvenanceConfig {

	@JsonProperty("externalReference")
	private String extRef;
	
	@JsonProperty("revOrganization")
	private String revOrg;
	
	private String revPerson;
	
	private String langCode;
	
	private String email;
	
	public void setExtRef(String extRef ){
		this.extRef = extRef;
	}
	
	public String getExtRef(){
		return extRef;
	}
	
	public void setRevOrg(String revOrg) {
		this.revOrg = revOrg;
	}
	
	public String getRevOrg(){
		return revOrg;
	}
	
	public void setRevPerson(String revPerson){
		this.revPerson = revPerson;
	}
	
	public String getRevPerson(){
		return revPerson;
	}
	
	public void setLangCode(String langCode){
		this.langCode = langCode;
	}
	
	public String getLangCode(){
		return langCode;
	}
	
	public void setEmail(String email){
		this.email = email;
	}
	
	public String getEmail(){
		return email;
	}
	
	@Override
	public String toString() {

		return "Reviewer: " + revPerson + ", email: " + email + ", organization: " + revOrg
				+ ", external ref: " + extRef + ", language code: " + langCode;
	}
	
}
