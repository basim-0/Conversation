package org.talkdesk.conversation.auth.impl.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import org.talkdesk.conversation.auth.impl.domain.model.Region
import org.talkdesk.conversation.auth.impl.domain.model.RegionalConfiguration
import org.talkdesk.conversation.auth.impl.domain.model.StoredAuth
import org.talkdesk.conversation.auth.impl.domain.model.Tokens

/** Persists the auth session in a plain (unencrypted) DataStore, as decided for this project. */
internal class DataStoreAuthStorage(private val dataStore: DataStore<Preferences>) : AuthStorage {

    override suspend fun read(): StoredAuth {
        val p = dataStore.data.first()
        val issuer = p[ISSUER]
        val gateway = p[GATEWAY]
        val access = p[ACCESS_TOKEN]
        val refresh = p[REFRESH_TOKEN]
        return StoredAuth(
            accountName = p[ACCOUNT],
            regional = if (issuer != null && gateway != null) {
                RegionalConfiguration(issuer, p[REGION]?.let(Region::valueOf) ?: Region.US, gateway)
            } else {
                null
            },
            tokens = if (access != null && refresh != null) {
                Tokens(access, refresh, p[SCOPE].orEmpty(), p[UPDATED_AT] ?: 0L)
            } else {
                null
            },
            refreshTokenExpired = p[REFRESH_EXPIRED] ?: false,
        )
    }

    override suspend fun saveAccount(accountName: String, regional: RegionalConfiguration) {
        dataStore.edit {
            it[ACCOUNT] = accountName
            it[ISSUER] = regional.issuer
            it[GATEWAY] = regional.gateway
            it[REGION] = regional.region.name
        }
    }

    override suspend fun saveRegional(regional: RegionalConfiguration) {
        dataStore.edit {
            it[ISSUER] = regional.issuer
            it[GATEWAY] = regional.gateway
            it[REGION] = regional.region.name
        }
    }

    override suspend fun saveTokens(tokens: Tokens) {
        dataStore.edit {
            it[ACCESS_TOKEN] = tokens.accessToken
            it[REFRESH_TOKEN] = tokens.refreshToken
            it[SCOPE] = tokens.scope
            it[UPDATED_AT] = tokens.updatedAtMillis
            it[REFRESH_EXPIRED] = false
        }
    }

    override suspend fun markRefreshTokenExpired() {
        dataStore.edit { it[REFRESH_EXPIRED] = true }
    }

    override suspend fun clearTokens() {
        dataStore.edit {
            it.remove(ACCESS_TOKEN)
            it.remove(REFRESH_TOKEN)
            it.remove(SCOPE)
            it.remove(UPDATED_AT)
            it.remove(REFRESH_EXPIRED)
        }
    }

    override suspend fun clearAccount() {
        dataStore.edit {
            it.remove(ACCOUNT)
            it.remove(ISSUER)
            it.remove(GATEWAY)
            it.remove(REGION)
        }
    }

    private companion object {
        val ACCOUNT = stringPreferencesKey("account_name")
        val ISSUER = stringPreferencesKey("issuer")
        val GATEWAY = stringPreferencesKey("gateway")
        val REGION = stringPreferencesKey("region")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val SCOPE = stringPreferencesKey("scope")
        val UPDATED_AT = longPreferencesKey("updated_at")
        val REFRESH_EXPIRED = booleanPreferencesKey("refresh_token_expired")
    }
}

internal interface AuthStorage {
    suspend fun read(): StoredAuth

    /** Saves the account and its (freshly looked up) regional configuration. */
    suspend fun saveAccount(accountName: String, regional: RegionalConfiguration)

    suspend fun saveRegional(regional: RegionalConfiguration)

    /** Saves the tokens and clears the refresh-expired flag. */
    suspend fun saveTokens(tokens: Tokens)

    suspend fun markRefreshTokenExpired()

    /** Removes the tokens and the expired flag. Keeps the account and regional configuration. */
    suspend fun clearTokens()

    /** Removes the remembered account and its regional configuration. */
    suspend fun clearAccount()
}
