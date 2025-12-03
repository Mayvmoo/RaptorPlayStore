package com.example.raptor.network

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * TrustManager die self-signed certificates accepteert voor development server
 * ⚠️ ALLEEN VOOR DEVELOPMENT - Gebruik certificate pinning voor productie!
 */
class SelfSignedTrustManager : X509TrustManager {
    companion object {
        private const val DEVELOPMENT_HOST = "57.131.28.13"
    }
    
    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        // Voor client certificaten - meestal niet nodig
    }
    
    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        // Accepteer self-signed certificates voor development server
        if (chain.isNullOrEmpty()) {
            throw java.security.cert.CertificateException("No certificate chain provided")
        }
        
        // Voor development: accepteer alle certificaten voor specifieke host
        // In productie: voeg certificate pinning toe
    }
    
    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return emptyArray()
    }
}

