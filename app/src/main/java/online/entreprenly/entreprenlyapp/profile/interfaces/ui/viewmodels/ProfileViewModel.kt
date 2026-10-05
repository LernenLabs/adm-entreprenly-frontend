package online.entreprenly.entreprenlyapp.profile.interfaces.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import online.entreprenly.entreprenlyapp.R
import online.entreprenly.entreprenlyapp.iam.application.queryservices.SessionQueryService
import online.entreprenly.entreprenlyapp.iam.domain.model.queries.GetCurrentSessionQuery
import online.entreprenly.entreprenlyapp.profile.application.acl.SubscriptionPlanFacade
import online.entreprenly.entreprenlyapp.profile.application.commandservices.ProfileCommandService
import online.entreprenly.entreprenlyapp.profile.application.queryservices.ProfileQueryService
import online.entreprenly.entreprenlyapp.profile.domain.model.aggregates.Profile
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdateNotificationSettingsCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdatePreferencesCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.commands.UpdateProfileCommand
import online.entreprenly.entreprenlyapp.profile.domain.model.queries.GetLocalPreferencesQuery
import online.entreprenly.entreprenlyapp.profile.domain.model.queries.GetProfileByUserIdQuery
import online.entreprenly.entreprenlyapp.profile.domain.model.valueobjects.Preferences
import online.entreprenly.entreprenlyapp.shared.application.result.ApplicationError
import online.entreprenly.entreprenlyapp.shared.application.result.Result
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.FormState
import online.entreprenly.entreprenlyapp.shared.interfaces.ui.UiText

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Loaded(val profile: Profile) : ProfileUiState
    /** US-62: the JWT is no longer valid, ask the user to sign in again. */
    data object SessionExpired : ProfileUiState
    data class Error(val message: UiText) : ProfileUiState
}

/**
 * Shared by all Profile screens (My profile, Edit, Preferences, Notifications) so they
 * always show the same, up-to-date profile.
 */
class ProfileViewModel(
    private val sessionQueryService: SessionQueryService,
    private val profileQueryService: ProfileQueryService,
    private val profileCommandService: ProfileCommandService,
    private val subscriptionPlanFacade: SubscriptionPlanFacade
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /** Preferences cached on the device; drives app theme and language. */
    val appPreferences: StateFlow<Preferences?> = profileQueryService.handle(GetLocalPreferencesQuery)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _profileUpdated = MutableStateFlow(false)
    /** True after a successful profile edit, until the banner is dismissed. */
    val profileUpdated: StateFlow<Boolean> = _profileUpdated.asStateFlow()

    private val _editForm = MutableStateFlow(FormState())
    val editForm: StateFlow<FormState> = _editForm.asStateFlow()

    private val _preferencesError = MutableStateFlow<UiText?>(null)
    val preferencesError: StateFlow<UiText?> = _preferencesError.asStateFlow()

    private val _notificationsForm = MutableStateFlow(FormState())
    val notificationsForm: StateFlow<FormState> = _notificationsForm.asStateFlow()

    private var userId: Long? = null

    /** Plan name from the subscription context; overrides the profile endpoint's stale `plan`. */
    private var planName: String? = null

    init {
        viewModelScope.launch {
            sessionQueryService.handle(GetCurrentSessionQuery).collect { session ->
                if (session == null) {
                    userId = null
                    planName = null
                    _uiState.value = ProfileUiState.Loading
                } else if (session.userId != userId) {
                    userId = session.userId
                    planName = null
                    load()
                }
            }
        }
    }

    fun load() {
        val id = userId ?: return
        _uiState.value = ProfileUiState.Loading
        viewModelScope.launch {
            val plan = async { subscriptionPlanFacade.currentPlanName(id) }
            val profile = profileQueryService.handle(GetProfileByUserIdQuery(id))
            val name = (plan.await() as? Result.Success)?.value
            if (userId != id) return@launch
            if (name != null) planName = name
            _uiState.value = when (profile) {
                is Result.Success -> loaded(profile.value)
                is Result.Failure -> profile.error.toUiState(UiText.Res(R.string.profile_load_error))
            }
        }
    }

    /** Re-reads the plan without reloading the profile, so a new subscription shows up right away. */
    fun refreshPlan() {
        val id = userId ?: return
        viewModelScope.launch {
            val name = (subscriptionPlanFacade.currentPlanName(id) as? Result.Success)?.value ?: return@launch
            if (userId != id) return@launch
            planName = name
            _uiState.update { state -> (state as? ProfileUiState.Loaded)?.let { loaded(it.profile) } ?: state }
        }
    }

    private fun loaded(profile: Profile): ProfileUiState =
        ProfileUiState.Loaded(planName?.let { profile.copy(plan = it) } ?: profile)

    fun dismissProfileUpdated() {
        _profileUpdated.value = false
    }

    fun resetEditForm() {
        _editForm.value = FormState()
    }

    /** US-63: update first name, last name and biography keeping the stored phone and avatar. */
    fun updateProfile(firstName: String, lastName: String, biography: String, onSuccess: () -> Unit) {
        val current = (uiState.value as? ProfileUiState.Loaded)?.profile ?: return
        if (firstName.isBlank()) {
            _editForm.value = FormState(error = UiText.Res(R.string.edit_first_name_required))
            return
        }
        _editForm.value = FormState(loading = true)
        viewModelScope.launch {
            val command = UpdateProfileCommand(
                profileId = current.id,
                firstName = firstName,
                lastName = lastName,
                biography = biography,
                phone = current.phone,
                avatarUrl = current.avatarUrl
            )
            when (val r = profileCommandService.handle(command)) {
                is Result.Success -> {
                    _uiState.value = loaded(r.value)
                    _editForm.value = FormState()
                    _profileUpdated.value = true
                    onSuccess()
                }
                is Result.Failure -> {
                    if (r.error is ApplicationError.Unauthorized) _uiState.value = ProfileUiState.SessionExpired
                    _editForm.value = FormState(error = UiText.Raw(r.error.message))
                }
            }
        }
    }

    /** US-67: the new preferences apply on the device immediately and are then sent to the backend. */
    fun updatePreferences(preferences: Preferences) {
        val current = (uiState.value as? ProfileUiState.Loaded)?.profile ?: return
        _preferencesError.value = null
        _uiState.value = ProfileUiState.Loaded(current.copy(preferences = preferences))
        viewModelScope.launch {
            when (val r = profileCommandService.handle(UpdatePreferencesCommand(current.id, preferences))) {
                is Result.Success -> _uiState.value = loaded(r.value)
                is Result.Failure -> {
                    _uiState.value = ProfileUiState.Loaded(current)
                    _preferencesError.value = UiText.Res(R.string.prefs_save_error)
                }
            }
        }
    }

    /** US-68: save the stock/expiration alert preference. */
    fun saveNotifications(stockAlerts: Boolean) {
        val current = (uiState.value as? ProfileUiState.Loaded)?.profile ?: return
        _notificationsForm.value = FormState(loading = true)
        viewModelScope.launch {
            _notificationsForm.value =
                when (val r = profileCommandService.handle(UpdateNotificationSettingsCommand(current.id, stockAlerts))) {
                    is Result.Success -> {
                        _uiState.value = loaded(r.value)
                        FormState(
                            success = UiText.Res(
                                if (stockAlerts) R.string.notif_saved_on else R.string.notif_saved_off
                            )
                        )
                    }
                    is Result.Failure -> FormState(error = UiText.Raw(r.error.message))
                }
        }
    }

    fun resetNotificationsForm() {
        _notificationsForm.update { FormState() }
    }

    private fun ApplicationError.toUiState(fallback: UiText): ProfileUiState = when (this) {
        is ApplicationError.Unauthorized -> ProfileUiState.SessionExpired
        is ApplicationError.Network -> ProfileUiState.Error(UiText.Raw(message))
        else -> ProfileUiState.Error(fallback)
    }
}
