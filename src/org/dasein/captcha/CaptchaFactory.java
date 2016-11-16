package org.dasein.captcha;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dasein.captcha.taglib.ChallengeTag;
import org.dasein.persist.PersistenceException;
import org.dasein.persist.PersistentCache;
import org.dasein.persist.RelationalCache;
import org.dasein.persist.SearchTerm;
import org.dasein.persist.jdbc.AutomatedSql.TranslationMethod;
import org.dasein.util.Cache;

/**
 * Allows a developer to implement captcha on their system.  They need to get 
 * access to a {@link Token} by getting one from the {@link ChallengeTag} or using
 * {@link CaptchaFactory#getToken()}. They then validate the user's answer by passing the
 * {@link Token#getTokenKey()} and <code>answer</code> to {@link CaptchaFactory#validateResponse(String, String)}.
 *  
 * @author Morgan Catlin <morgan.catlin@valtira.com>
 *
 */
public class CaptchaFactory {
	private static final Logger logger = Logger.getLogger(CaptchaFactory.class);
	
    static private final Map<String,Map<String,Number>> numbers = new HashMap<String,Map<String,Number>>();
    
    static {
        // TODO: move this into properties files
        HashMap<String,Number> map = new HashMap<String,Number>();
        
        numbers.put("en", map);
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        map.put("four", 4);
        map.put("five", 5);
        map.put("six", 6);
        map.put("seven", 7);
        map.put("eight", 8);
        map.put("nine", 9);
        map.put("ten", 10);
        
        map = new HashMap<String,Number>();
        numbers.put("fr", map);
        map.put("un", 1);
        map.put("une", 1);
        map.put("deux", 2);
        map.put("trois", 3);
        map.put("quatre", 4);
        map.put("cinq", 5);
        map.put("six", 6);
        map.put("sept", 7);
        map.put("huit", 8);
        map.put("neuf", 9);
        map.put("dix", 10);
    }
    
	static private final SecureRandom srand = new SecureRandom();
    static private final Random       rand  = new Random();
	
	private static CaptchaFactory instance = new CaptchaFactory();
	
	RelationalCache<Challenge> challenges = null;
	RelationalCache<Response> responses = null;
	
	@SuppressWarnings("unchecked")
	private CaptchaFactory() {		
		
		super();
        
        try {
        	challenges = (RelationalCache<Challenge>) PersistentCache.getCache(Challenge.class, Challenge.CHALLENGE_ID);
        	challenges.setTranslationMethod(TranslationMethod.NONE);
    	} catch (PersistenceException e) {
    		logger.warn(e.getMessage(), e);
    	}	
        
        try {
        	responses = (RelationalCache<Response>) PersistentCache.getCache(Response.class, Response.RESPONSE_ID);
        	responses.setTranslationMethod(TranslationMethod.NONE);
    	} catch (PersistenceException e) {
    		logger.warn(e.getMessage(), e);
    	}	
	}
	
	public static CaptchaFactory getInstance() {
		return instance;
	}

	Cache<TreeSet<Token>> tokens = new Cache<TreeSet<Token>>();
	
	public Challenge getChallenge(long challengeId) throws PersistenceException {
		return challenges.get(challengeId);
	}
	
	public Challenge getUnusedChallenge(long sessionId, String language) throws PersistenceException {
		if (logger.isDebugEnabled()) {
			logger.debug("getUnusedChallenge() - enter");
			logger.debug(tokens);
		}
		try {
			ArrayList<Challenge> cs = new ArrayList<Challenge>();
			cs.addAll(challenges.find(new SearchTerm(Challenge.LANGUAGE, language)));	
            if( cs.isEmpty() ) {
                return null;
            }
			Collections.shuffle(cs);
			
			boolean found = false;
			
			TreeSet<Token> tks = tokens.get(sessionId);
			if (tks == null) {
				tks = new TreeSet<Token>();
				tokens.cache(sessionId, tks);
			}
						
			for (Challenge c : cs) {
				for (Token t : tks) {
					if (t.getChallenge().equals(c)) {
						found = true;
						break;					
					}
				}
				if (!found) {
					return c;
				}
				found = false;
			}
			return null;
		} finally {
			if (logger.isDebugEnabled()) {
				logger.debug("getUnusedChallenge() - exit");
				logger.debug(tokens);
			}
		}
	}
	
    private long getNumber(String input, String language) throws NumberFormatException {
        Map<String,Number> specific = numbers.get(language);
        Number num;
        
        if( specific == null && !language.equals("en") ) {
            return getNumber(input, "en");
        }
        num = specific.get(input);
        if( num != null ) {
            return num.longValue();
        }
        throw new NumberFormatException(input);
    }
    
	public Response getResponse(Challenge challenge) throws PersistenceException {
		Collection<Response> resps =  responses.find(new SearchTerm(Response.CHALLENGE, challenge.getChallengeId()));
		if (!resps.isEmpty()) {
			return resps.iterator().next();
		}
		return null;
	}
	
	public Response getResponse(long responseId) throws PersistenceException {
		return responses.get(responseId);
	}
	
	public Token getToken(long sessionId, Collection<String> languages) throws PersistenceException {
		StringBuilder buff = new StringBuilder();
        int count = 10 + rand.nextInt(5);
        byte[] data = new byte[20];
                
        while( buff.length() < count ) {
            srand.nextBytes(data);
            for(int i=0; i<data.length; i++) {
                char c = (char)data[i];
                
                if( c == 'l' || c == 'i' || c == '1' || c == 'I' ) {
                    continue;
                }
                if( c >= 'a' && c <= 'z' ) {
                    buff.append(c);
                }
                else if( c >= 'A' && c <= 'Z' ) {
                    buff.append(c);
                }
                else if( c >= '0' && c <= '9' ) {
                    buff.append(c);
                }
                if( buff.length() >= count ) {
                    break;
                }
            }
        }
        
        Challenge challenge = null;
        
        for( String lang : languages ) {
            challenge = getUnusedChallenge(sessionId, lang);
            if( challenge != null ) {
                break;
            }
        }
        if (challenge == null) {
            throw new PersistenceException("No unused challenges found, perhaps you need more?");
        }
        
        Token token = new Token(buff.toString(), challenge);
        
        // cache
        TreeSet<Token> tks = tokens.get(sessionId);
        tks.add(token);
        
        return token;
	}
	
    private String clean(String input) {
        StringBuffer clean = new StringBuffer();

        for( int i = 0; i<input.length(); i++ ) {
            char c = input.charAt(i);
            
            if( Character.isLetterOrDigit(c) ) {
                switch( c ) {
                case 'à': case 'â': case 'á': case 'ã': case 'ä': case 'å': c = 'a'; break;
                case 'ç': case 'č': c = 'c'; break;
                case 'é': case 'è': case 'ê': c = 'e'; break;
                case 'î': case 'ì': c = 'i'; break;
                case 'ñ': c = 'n'; break;
                case 'ß': clean.append('s'); c = 's'; break; 
                case 'ô': c = 'o'; break;
                case 'û': case 'ù': c = 'u'; break;   
                }
                clean.append(c);
            }
        }
        return clean.toString();
    }
    
	/**
	 * Validates a user's answer.
	 * 
	 * @param tokenKey the key of the {@link Token}.
	 * @param answer the answer to verify.
	 * @return whether the answer is validated.
	 * @throws PersistenceException thrown if there's an issue.
	 */
	public boolean validateResponse(long sessionId, String tokenKey, String answer) throws PersistenceException {
        answer = clean(answer.toLowerCase());
		boolean success = false;
		Token token = null;
		try {
			TreeSet<Token> tks = tokens.get(sessionId);
			if (tks == null || tks.size() == 0) {
				return false;
			}
			
			token = tks.first();
			
			Response resp = token.getChallenge().getResponse();
			
			try {
			    long num = Long.parseLong(resp.getAnswer());
                
                try {
                    long other = Long.parseLong(answer);
                    
                    success = (other == num);
                }
                catch( NumberFormatException e ) {
                    try {
                        long other = getNumber(answer, token.getChallenge().getLanguage());
                        
                        success = (other == num);
                    }
                    catch( NumberFormatException again ) {
                        success = false;
                    }
                }
            }
            catch( NumberFormatException e ) {
                success = resp.getAnswer().equalsIgnoreCase(answer);
            }
		} finally {
			if (success) {
				tokens.release(sessionId);
			}
		}
		return success;
	}
}
