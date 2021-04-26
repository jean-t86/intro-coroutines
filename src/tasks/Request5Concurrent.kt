package tasks

import contributors.*
import kotlinx.coroutines.*

suspend fun loadContributorsConcurrent(service: GitHubService, req: RequestData): List<User> = coroutineScope {
    val repos = service
        .getOrgReposCall(req.org)
        .also { logRepos(req, it) }
        .body() ?: listOf()

    val deferreds: List<Deferred<List<User>>> = repos.map { repo ->
        async {
            log("starting loading for ${repo.name}")
            service.getRepoContributorsCall(req.org, repo.name)
                .also { logUsers(repo, it) }
                .bodyList()
            // load contributors for each repo
        }
    }
    deferreds.awaitAll().flatten().aggregate()
}
