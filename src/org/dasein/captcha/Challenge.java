package org.dasein.captcha;

import java.io.Serializable;
import java.util.Date;

import org.dasein.persist.PersistenceException;
import org.dasein.util.CachedItem;

public class Challenge implements CachedItem, Serializable {
	
	private static final long serialVersionUID = -6809618499672319900L;

	public static final String CHALLENGE_ID = "challengeId";
	static public final String LANGUAGE = "language";
    
	private Number challengeId = null;
	private Boolean active = null;
	private Number created = null;
	private String createdBy = null;
    private String language  = null;
	private Number lastModified = null;
	private String lastModifiedBy = null;
	private String question = null;
		
    public Challenge() { } 
    
	public long getChallengeId() {
		return challengeId.longValue();
	}
	
	public boolean isActive() {
		return active;
	}
	
	public long getCreated() {
		return created.longValue();
	}
	
	public Date getCreatedAsDate() {
		return new Date(created.longValue());
	}
	
	public String getCreatedBy() {
		return createdBy;
	}
	
    public String getLanguage() { 
        return language;
    }
    
	public long getLastModified() {
		return lastModified.longValue();
	}
	
	public Date getLastModifiedAsDate() {
		return new Date(lastModified.longValue());
	}
	
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	
	public String getQuestion() {
		return question;
	}
	
	public Response getResponse() throws PersistenceException {
		return CaptchaFactory.getInstance().getResponse(this);
	}
	
	public boolean isValidForCache() {
	    return true;
    } 
	
	public boolean equals(Object ob) {
		Challenge other;
        
        if( ob == null ) {
            return false;
        }
        if( ob == this ) {
            return true;
        }
        if( !getClass().getName().equals(ob.getClass().getName()) ) {
            return false;
        }
        other = (Challenge)ob;
        return (challengeId.longValue() == other.challengeId.longValue());
    }
}
