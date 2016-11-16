package org.dasein.captcha.taglib;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.dasein.captcha.CaptchaFactory;
import org.dasein.persist.PersistenceException;

import com.valtira.cms.client.CMS;

public class ChallengeTag extends TagSupport {

	private static final long serialVersionUID = -4065765224383751149L;
	
	private String var = null;
	private String varError = null;
		
	@SuppressWarnings("unchecked")
    @Override
	public int doEndTag() throws JspException {
		try {		
			CMS cms = (CMS) pageContext.getSession().getAttribute(CMS.CMS);
			long sessionId;
			if (cms != null && cms.getSession() != null) {
				sessionId = cms.getSession().getSessionId();
			} else {
				// probably a bot - this will not allow anyone who doesn't have a valid session to post
				sessionId = -1;
			}
            ArrayList<String> languages = new ArrayList<String>();
            
            Enumeration<Locale> locales = pageContext.getRequest().getLocales();
            if( locales != null ) {
                while( locales.hasMoreElements() ) {
                    Locale l = locales.nextElement();
                    
                    if( l != null ) {
                        String lang = l.getLanguage();
                        
                        if( lang != null ) {
                            if( !languages.contains(lang) ) {
                                languages.add(lang);
                            }
                        }
                    }
                }
            }
            languages.add("en");

			pageContext.setAttribute(var, 
					CaptchaFactory.getInstance().getToken(sessionId, languages));
		} catch (PersistenceException e) {
			pageContext.setAttribute(varError, e.getMessage());
		} finally {
			var = null;
		}
		return EVAL_PAGE;
	}

	public void setVar(String var) {
		this.var = var;
	}	
	
	public void setVarError(String varError) {
		this.varError = varError;
	}
}
