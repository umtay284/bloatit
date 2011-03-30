package com.bloatit.web.linkable.money;

import java.math.BigDecimal;

import com.bloatit.framework.webserver.WebProcess;
import com.bloatit.framework.webserver.annotations.Optional;
import com.bloatit.framework.webserver.annotations.ParamContainer;
import com.bloatit.framework.webserver.annotations.RequestParam;
import com.bloatit.framework.webserver.url.Url;
import com.bloatit.web.url.AccountChargingPageUrl;
import com.bloatit.web.url.AccountChargingProcessUrl;

@ParamContainer("account/charging/process")
public class AccountChargingProcess extends WebProcess {

    public static final String CHARGE_AMOUNT_CODE = "amount";

    @Optional
    @RequestParam(name = CHARGE_AMOUNT_CODE, role = RequestParam.Role.GET)
    private BigDecimal amount;


    private final AccountChargingProcessUrl url;

    public AccountChargingProcess(AccountChargingProcessUrl url) {
        super(url);
        this.url = url;
        amount = url.getAmount();
    }



    public BigDecimal getAmount() {
        return amount;
    }


    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }


    @Override
    protected Url doProcess() {
        return new AccountChargingPageUrl(this);
    }

    @Override
    protected Url doProcessErrors() {
        session.notifyList(url.getMessages());
        return session.getLastVisitedPage();
    }

    @Override
    public void load() {
    }

}