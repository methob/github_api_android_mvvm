package com.br.agile_github.agilegithubapi.ui.userRepositories

import android.content.Intent
import android.view.View
import com.br.agile_github.agilegithubapi.ProjectApplication
import com.br.agile_github.agilegithubapi.R
import com.br.agile_github.agilegithubapi.databinding.ActivityUserRepositoryBinding
import com.br.agile_github.agilegithubapi.model.Repository
import com.br.agile_github.agilegithubapi.model.User
import com.br.agile_github.agilegithubapi.ui.base.BaseActivity
import com.br.agile_github.agilegithubapi.ui.detailRepositories.DetailRepositoryActivity
import com.br.agile_github.agilegithubapi.ui.userRepositories.adapter.RepositoryAdapter
import com.br.agile_github.agilegithubapi.ui.userRepositories.di.UserRepositoryModule
import com.br.agile_github.agilegithubapi.utils.Constants
import com.br.agile_github.agilegithubapi.utils.SimpleDividerItemDecoration
import javax.inject.Inject

/**
 * A group of *ui/userRepositories*.
 *
 * Activity that lists user repositories.
 *
 */
class UserRepositoryActivity : BaseActivity<ActivityUserRepositoryBinding, UserRepositoryViewModel>() {

    /**
     * Inject Adapter to [UserRepositoryActivity].
     */
    @Inject
    lateinit var adapter: RepositoryAdapter

    /**
     * Inject line divider to [UserRepositoryActivity].
     */
    @Inject
    lateinit var itemDividerDecoration: SimpleDividerItemDecoration

    /**
     * create [lazy] variable to receive [User] from intent params.
     */
    private val user by lazy { intent.getParcelableExtra<User>(Constants.USER_REPOSITORY_PARAM) }

    /**
     * Inject the current Activity
     */
    override fun initInjector() {
        ProjectApplication.graph.injectSub(UserRepositoryModule(user, this)).injectTo(this)
    }

    /**
     * inflate the Activity.
     * @return layout of current Activity.
     */
    override fun initContentView(): Int {
        return R.layout.activity_user_repository
    }

    fun onBack(v: View) {
        onBackPressed()
    }

    /**
     * initiate ui configurations.
     */
    override fun initUI() {
        mDataBinding?.viewModel = mViewModel
//        setSupportActionBar(mDataBinding?.toolbar)
//        enableToolbarBackButton()
        configureRequestRxResponse()

        mViewModel.fetchRepositories()
    }

    /**
     * Configure [adapter].
     */
    private fun setupAdapter(repository: List<Repository>) {

        if (repository.isEmpty())
            mDataBinding!!.txtEmptyRepositories.visibility = View.VISIBLE

        adapter.updateRepository(repository)
        mDataBinding!!.recyclerView.adapter = adapter
        mDataBinding!!.recyclerView.addItemDecoration(itemDividerDecoration)

        adapter.setClickListener {
            callDetailActivity(it)
        }
    }

    /**
     * Call [DetailRepositoryActivity].
     */
    private fun callDetailActivity(repository: Repository) {
        val intent = Intent(this, DetailRepositoryActivity::class.java)
        intent.putExtra(Constants.USER_REPOSITORY_DETAIL_PARAM, repository)
        intent.putExtra(Constants.USER_REPOSITORY_PARAM, user)
        startActivity(intent)
    }

    /**
     * Method responsible for dealing with all events published during the request
     */
    private fun configureRequestRxResponse() {

        disposables.add(mViewModel.getRepositories().subscribe {
            mDataBinding!!.progressBar.visibility = View.INVISIBLE
            setupAdapter(it)
        })

        disposables.add(mViewModel.remoteErrors().subscribe {

            mDataBinding!!.progressBar.visibility = View.INVISIBLE
            mViewModel.dialogUtils.showAlertDialog(it.message!!, getString(R.string.txt_connection_error))
        })

        disposables.add(mViewModel.fetchErrors().subscribe {

            mDataBinding!!.progressBar.visibility = View.INVISIBLE
            mViewModel.dialogUtils.showAlertDialog(it.message!!, getString(R.string.txt_default_error))
        })
    }
}
