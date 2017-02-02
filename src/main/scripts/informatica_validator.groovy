/**
 * © Copyright IBM Corporation 2015, 2017.
 * This is licensed under the following license.
 * The Eclipse Public 1.0 License (http://www.eclipse.org/legal/epl-v10.html)
 * U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

if (password && passwordscript) {
    errors.password = "Only Password or Password Script can be entered."
    errors.passwordscript = "Only Password or Password Script can be entered."
}
if (!password && !passwordscript) {
    errors.password = "Password or Password Script is required."
    errors.passwordscript = "Password or Password Script is required."
}

/*
if (domain && (host || port)) {
    errors.domain = "Only Domain or Host and Port can be entered."
    errors.host = "Only Domain or Host and Port can be entered."
    errors.port = "Only Domain or Host and Port can be entered."
}
else if (!domain && (!host || !port)) {
    errors.domain = "Domain or Host and Port is required."
    errors.host = "Domain or Host and Port is required."
    errors.port = "Domain or Host and Port is required."
}
else if (!domain && !host && !port) {
    errors.domain = "Domain or Host and Port is required."
    errors.host = "Domain or Host and Port is required."
    errors.port = "Domain or Host and Port is required."
}
else if (port) {
    try {
        int portNumber = Integer.parseInt(port)
        if (portNumber < 1 || portNumber > 65536) {
            errors.port = "Port must be a valid port number."
        }
    }
    catch (Exception e) {
        errors.port = "Port must be a valid port number."
    }
}
*/
