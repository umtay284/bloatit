# -*- coding: utf-8 -*-

# Copyright (C) 2010 BloatIt.
#
# This file is part of BloatIt.
#
# BloatIt is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# BloatIt is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with BloatIt. If not, see <http://www.gnu.org/licenses/>.

from bloatit.web.htmlrenderer.pagecontent.logincontent import LoginContent
from bloatit.web.htmlrenderer.pagecontent.indexcontent import IndexContent
from bloatit.web.actions.action import Action
from bloatit.web.htmlrenderer.htmltools import HtmlTools
from bloatit.framework.loginmanager import LoginManager


class LoginAction(Action):

    def get_code(self):
        return "login"

    def get_login_code(self):
        return "bloatit_login"

    def get_password_code(self):
        return "bloatit_password"

    def process(self, html_result, query, post):
        # @type html_result HtmlResult
        
        if self.get_login_code() in post and self.get_password_code() in post:
            login = post[self.get_login_code()][0]
            password = post[self.get_password_code()][0]

            auth_token = LoginManager.login_by_password(login, password)

            if auth_token:
                self.session.set_logged(True)
                self.session.set_auth_token(auth_token)
                html_result.set_redirect(HtmlTools.generate_url(self.session,IndexContent(self.session)))
            else:
                self.session.set_logged(False)
                self.session.set_auth_token(None)
                html_result.set_redirect(HtmlTools.generate_url(self.session,LoginContent(self.session)))


        
        