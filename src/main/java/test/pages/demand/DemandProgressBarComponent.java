/*
 * Copyright (C) 2010 BloatIt.
 * 
 * This file is part of BloatIt.
 * 
 * BloatIt is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * BloatIt is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with
 * BloatIt. If not, see <http://www.gnu.org/licenses/>.
 */
package test.pages.demand;

import test.HtmlElement;
import test.pages.components.HtmlBlock;
import test.pages.components.HtmlProgressBar;
import test.pages.components.HtmlText;
import test.pages.demand.DemandPage.Request;

public class DemandProgressBarComponent extends HtmlElement {

    private float progressValue;
    private HtmlText totalText;

    public DemandProgressBarComponent(Request request) {
        super();
        extractData(request);
        add(produce(request));
    }

    protected HtmlElement produce(Request request) {

        final HtmlBlock progressBlock = new HtmlBlock("progress_block");
        {

            progressBlock.add(new DemandContributeButtonComponent(request));

            final HtmlBlock progressBarBlock = new HtmlBlock("column");
            {
                progressBarBlock.add(new HtmlProgressBar(progressValue));
            }

            progressBlock.add(progressBarBlock);

            progressBlock.add(new DemandMakeOfferButtonComponent(request));

        }

        return progressBlock;
    }

    protected void extractData(Request request) {
        progressValue = 0;
        progressValue = 42 * (1 - 1 / (1 + request.demand.getContribution().floatValue() / 200));

        totalText = new HtmlText(request.demand.getContribution().toPlainString() + "€");

    }
}
