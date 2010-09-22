# -*- coding: utf-8 -*-

# Copyright (C) 2009-2010 Frédéric Bertolus.
# Copyright (C) 2009-2010 Matthieu Bizien.
#
# This file is part of Perroquet.
#
# Perroquet is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Perroquet is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Perroquet.  If not, see <http://www.gnu.org/licenses/>.

import gettext
import os
import sys

from bloatit.config.config_lib import Config

APP_NAME = 'bloatit'
APP_VERSION = '1.0.0'

config = Config()

#General
config.set("version", APP_VERSION)
config.set("app_name", APP_NAME)
config.set("script", sys.path[0])
config.set("root_path", sys.path[0]+'/../../../')

#path locale
if os.path.exists(os.path.join(config.get("root_path"), 'build/mo')):
    config.set("localedir", os.path.join(config.get("root_path"), 'locales'))
else:
    print("ERROR : no locales path")
    

#gettext
config.set("gettext_package", "bloatit")
#gettext.install (config.get("gettext_package"), config.get("localedir"))
