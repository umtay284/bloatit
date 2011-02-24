package com.bloatit.web.pages.documentation;

import com.bloatit.web.pages.documentation.HtmlDocumentationRenderer.DocumentationType;
import com.bloatit.web.pages.master.SideBarElementLayout;

public class SideBarDocumentationBlock extends SideBarElementLayout {

    public SideBarDocumentationBlock(String key) {
        add(new HtmlDocumentationRenderer(DocumentationType.FRAME, key));
    }

}
