# Marquardt Java SDK
[![Build Status](https://api.travis-ci.org/echocat/marquardt-java-sdk.svg?branch=master)](https://travis-ci.org/echocat/marquardt-java-sdk)
[![Coverage Status](https://coveralls.io/repos/echocat/marquardt-java-sdk/badge.svg?branch=master&service=github)](https://coveralls.io/github/echocat/marquardt-java-sdk?branch=master)
[![Dependency Status](https://www.versioneye.com/user/projects/55f2c1ced4d2040019000161/badge.svg?style=flat)](https://www.versioneye.com/user/projects/55f2c1ced4d2040019000161)
[![License](https://img.shields.io/badge/license-MPL%202.0-blue.svg)](http://mozilla.org/MPL/2.0/)

Echocat Marquardt is a certificate based distributed authentication / authorization framework. 

It uses a central authority which holds the session of a user. The authority produces certificates which are signed using a key pair with a public key trusted by Marquardt services and clients. Certificates have a short time to live of ~ 15 minutes. When they are expired, they must be refreshed.

Clients communicate with the authority to obtain a certificate. They can use the certificate to read the roles of a user to allow access to client features and to get basic user information for presentation. They also use the certificate to access protected services.

Services can authenticate and authorize client requests without roundtrips to the central authority. They read the certificate from the request header and check the requests are also signed by the client (whose public key is contained in the certificate). They use embedded payload from the certificate to access necessary user information.

## Authority
[![Dependency Status](https://www.versioneye.com/user/projects/55f2c44ad4d204001c00011d/badge.svg?style=flat)](https://www.versioneye.com/user/projects/55f2c44ad4d204001c00011d)

## Client
[![Dependency Status](https://www.versioneye.com/user/projects/55f2c431d4d204001c000118/badge.svg?style=flat)](https://www.versioneye.com/user/projects/55f2c431d4d204001c000118)

## Service
[![Dependency Status](https://www.versioneye.com/user/projects/55f2c432d4d204001e0000c7/badge.svg?style=flat)](https://www.versioneye.com/user/projects/55f2c432d4d204001e0000c7)

## Common
[![Dependency Status](https://www.versioneye.com/user/projects/55f2c543d4d2040019000197/badge.svg?style=flat)](https://www.versioneye.com/user/projects/55f2c543d4d2040019000197)

## Example
[![Dependency Status](https://www.versioneye.com/user/projects/55f2c431d4d2040019000185/badge.svg?style=flat)](https://www.versioneye.com/user/projects/55f2c431d4d2040019000185)

## Setup project

Please activate the provided pre-commit hook to ensure no files without license information are committed. To activate the hook, please run:

```
cd .git_hooks
./enable_hooks.sh
```

## LICENSE

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, You can obtain one at http://mozilla.org/MPL/2.0/.
