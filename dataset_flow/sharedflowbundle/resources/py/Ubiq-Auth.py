import hmac
import hashlib
import base64
import time
import email.utils
import urllib

VERSION = "2.2.0"

class Http_Auth:
    """HTTP Authentication for the Ubiq Platform

    This module implements HTTP authentication for the Ubiq platform
    via message signing as described by the IETF httpbis-message-signatures
    draft specification.

    This module is rewritten from the ubiq-python library for use with Apigee.
    """
    def __init__(self, access_id, access_key):
        """
        access_id:
            a string containing the client's public API key (pAPI)
        access_key:
            a string containing the client's secret API key (sAPI)
        """

        self.access_id = access_id
        self.access_key = access_key

    def headers(self, method, query):
        body = False

        rheaders = {}

        rheaders['user-agent'] = 'ubiq-apigee/' + VERSION

        # the '(request-target)' is part of the signed data.
        # it's value is 'http_method path?query'

        rheaders['content-type'] = 'application/json'
        req_tgt = method.lower() + ' /api/v0/fpe/def_keys'
        if query:
            req_tgt += '?' + query

        # the time at which the signature was created
        # expressed as the unix epoch

        created = str(int(time.time()))

        # the requests library doesn't typically add the Host
        # header. it needs to be present to be part of the
        # signature. the port value is not included if it is
        # the default port for the scheme

        rheaders['host'] = 'api.ubiqsecurity.com'

        # the Date field is required for the signature

        if not rheaders.get('date'):
            rheaders['date'] = email.utils.formatdate(
                timeval=None, localtime=False, usegmt=True)

        # the Digest header is always included/overridden by
        # this code. it is a hash of the body of the http message
        # and is always present even if the body is empty
        hash_sha512 = hashlib.sha512()

        if body:
            hash_sha512.update(body)

        rheaders['digest']  = 'SHA-512='
        rheaders['digest'] += base64.b64encode(
            hash_sha512.digest()).decode('utf-8')

        #
        # sign the message. the signature is an hmac of the
        # headers listed below
        #
        hmac_sha512 = hmac.new(self.access_key, '', hashlib.sha512)

        header_names = []

        # the (request-target) and (created) are faux headers defined
        # by the message signature spec. they are added to the list
        # of "real" headers to make the code below simpler and then
        # removed from the request after the hmac has been updated

        rheaders['(request-target)'] = req_tgt
        rheaders['(created)'] = created

        # include the specified headers in the hmac calculation. each
        # header is of the form 'header_name: header value\n'
        #
        # included headers are also added to an ordered list of headers
        # which is included in the message

        for name in ['content-type', 
                     'date', 
                     'host', 
                     '(created)', 
                     '(request-target)', 
                     'digest']:
            if rheaders.get(name):
                header_names.append(name.lower())
                hmac_sha512.update(
                    (name.lower() + ': ' + rheaders[name] + "\n").
                    encode('utf-8'))

        del rheaders['(created)']
        del rheaders['(request-target)']

        # build the signature header itself

        rheaders['signature']  = 'keyId="' + self.access_id + '"'
        rheaders['signature'] += ', algorithm="hmac-sha512"'
        rheaders['signature'] += ', created=' + created
        rheaders['signature'] += ', headers="' + ' '.join(header_names) + '"'
        rheaders['signature'] += ', signature="'
        rheaders['signature'] += base64.b64encode(
            hmac_sha512.digest()).decode('utf-8')
        rheaders['signature'] += '"'

        return rheaders

papi = flow.getVariable('ACCESS_KEY_ID')
sapi = flow.getVariable('SECRET_SIGNING_KEY')
dataset_name = flow.getVariable('dataset_name')

# Apigee will encode the papi in request, so make sure signature uses encoded too.
encoded_papi = urllib.quote_plus(papi)
query = "papi="+encoded_papi+"&ffs_name="+dataset_name

auth = Http_Auth(papi, sapi)
headers = auth.headers('GET', query)

for key in headers:
    flow.setVariable('requestDatasetKey.header.'+key, headers[key])