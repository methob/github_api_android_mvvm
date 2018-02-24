package com.br.agile_github.agilegithubapi.ui.searchUser

import android.content.Context
import com.br.agile_github.agilegithubapi.R
import com.br.agile_github.agilegithubapi.data.network.NetworkInteractor
import com.br.agile_github.agilegithubapi.data.remote.GithubApi
import com.br.agile_github.agilegithubapi.model.ErrorBodyRequisition
import com.br.agile_github.agilegithubapi.model.User
import com.br.agile_github.agilegithubapi.ui.base.BaseViewModel
import com.br.agile_github.agilegithubapi.utils.DialogUtils
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class SearchUserViewModel @Inject constructor(
        val apiService: GithubApi,
        val networkInteractor: NetworkInteractor,
        val context: Context) : BaseViewModel() {

    private var networkRequest: Disposable = Disposables.disposed()

    private var repos: BehaviorSubject<User> = BehaviorSubject.create()

    fun fetchUser(text: String) {

        dialogUtils.showOrHideProgressDialog()

        networkRequest =
                networkInteractor.hasNetworkConnectionCompletable()
                        .andThen(validateSearchString(text))
                        .andThen(apiService.getUser(text))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe {
                            networkRequest.dispose() // Cancel any current running request
                        }
                        .subscribeWith(object : DisposableSingleObserver<User>() {
                            override fun onError(e: Throwable) {

                                dialogUtils.showOrHideProgressDialog()

                                when (e) {
                                    is NetworkInteractor.NetworkUnavailableException -> networkErrors.onNext(e)
                                    else -> fetchErrors.onNext(getMessageErrorBody(e))
                                }
                            }

                            override fun onSuccess(value: User) {

                                dialogUtils.showOrHideProgressDialog()
                                repos.onNext(value)
                            }
                        })
    }

    private fun validateSearchString(text: String): Completable {
        return if (text.isEmpty()) Completable.error(Throwable(context.getString(R.string.txt_err_search_empty))) else Completable.complete()
    }

    fun getUser(): Observable<User> = repos.hide()
}