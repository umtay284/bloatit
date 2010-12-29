package com.bloatit.web.utils.url;

import com.bloatit.web.annotations.Message.Level;
import com.bloatit.web.annotations.RequestParam.Role;
import com.bloatit.web.utils.url.UrlParameter;
import com.bloatit.web.utils.annotations.Loaders;
import com.bloatit.web.utils.annotations.Loaders.*;
import com.bloatit.web.exceptions.RedirectException;

@SuppressWarnings("unused")
public class OfferActionUrl extends Url {
public static String getName() { return "action/offer"; }
public com.bloatit.web.actions.OfferAction createPage() throws RedirectException{ 
    return new com.bloatit.web.actions.OfferAction(this); }
public OfferActionUrl(Parameters params, Parameters session) {
    this();
    parseParameters(params, false);
    parseParameters(session, true);
}
public OfferActionUrl(){
    super(getName());
}


@Override 
protected void doRegister() { 
}

public OfferActionUrl clone() { 
    OfferActionUrl other = new OfferActionUrl();
    return other;
}
}