#!/usr/bin/env python3
from flask import Flask, request, abort

instance_url = "http://localhost:5000"
app = Flask(__name__)

num_assets = 0

def next_aid():
    global num_assets
    aid = num_assets * 400 + 1000000
    num_assets += 1
    return aid

@app.route("/repos/<user>/<repo>")
def e_root(user, repo):
    return f"""{{
            "id": 0,
            "node_id": "xxxxxx",
            "name": "{repo}",
            "full_name": "{user}/{repo}",
            "private": false,
            "owner": {{
                "login": "{user}",
                "id": 0,
                "node_id": "xxxxxxxxxxxxxxx",
                "avatar_url": "",
                "gravatar_id": "",
                "url": "{instance_url}/users/{user}",
                "html_url": "{instance_url}/{user}",
                "followers_url": "{instance_url}/users/{user}/followers",
                "following_url": "{instance_url}/users/{user}/following{{/other_user}}",
                "gists_url": "{instance_url}/users/{user}/gists{{/gist_id}}",
                "starred_url": "{instance_url}/users/{user}/starred{{/owner}}{{/repo}}",
                "subscriptions_url": "{instance_url}/users/{user}/subscriptions",
                "organizations_url": "{instance_url}/users/{user}/orgs",
                "repos_url": "{instance_url}/users/{user}/repos",
                "events_url": "{instance_url}/users/{user}/events{{privacy}}",
                "received_events_url": "{instance_url}/users/{user}/received_events",
                "type": "User",
                "site_admin": false
            }},
            "html_url": "{instance_url}/{user}/{repo}",
            "description": "A github repository",
            "fork": false,
            "url": "{instance_url}/repos/{user}/{repo}",
            "forks_url": "{instance_url}/repos/{user}/{repo}/forks",
            "keys_url": "{instance_url}/repos/{user}/{repo}/keys{{/key_id}}",
            "collaborators_url": "{instance_url}/repos/{user}/{repo}/collaborators{{/collaborator}}",
            "teams_url": "{instance_url}/repos/{user}/{repo}/teams",
            "hooks_url": "{instance_url}/repos/{user}/{repo}/hooks",
            "issue_events_url": "{instance_url}/repos/{user}/{repo}/issues/events{{/number}}",
            "events_url": "{instance_url}/repos/{user}/{repo}/events",
            "assignees_url": "{instance_url}/repos/{user}/{repo}/assignees{{/user}}",
            "branches_url": "{instance_url}/repos/{user}/{repo}/branches{{/branch}}",
            "tags_url": "{instance_url}/repos/{user}/{repo}/tags",
            "blobs_url": "{instance_url}/repos/{user}/{repo}/git/blobs{{/sha}}",
            "git_tags_url": "{instance_url}/repos/{user}/{repo}/git/tags{{/sha}}",
            "git_refs_url": "{instance_url}/repos/{user}/{repo}/git/refs{{/sha}}",
            "trees_url": "{instance_url}/repos/{user}/{repo}/git/trees{{/sha}}",
            "statuses_url": "{instance_url}/repos/{user}/{repo}/statuses{{/sha}}",
            "languages_url": "{instance_url}/repos/{user}/{repo}/languages",
            "stargazers_url": "{instance_url}/repos/{user}/{repo}/stargazers",
            "contributors_url": "{instance_url}/repos/{user}/{repo}/contributors",
            "subscribers_url": "{instance_url}/repos/{user}/{repo}/subscribers",
            "subscription_url": "{instance_url}/repos/{user}/{repo}/subscription",
            "commits_url": "{instance_url}/repos/{user}/{repo}/commits{{/sha}}",
            "git_commits_url": "{instance_url}/repos/{user}/{repo}/git/commits{{/sha}}",
            "comments_url": "{instance_url}/repos/{user}/{repo}/comments{{/number}}",
            "issue_comment_url": "{instance_url}/repos/{user}/{repo}/issues/comments{{/number}}",
            "contents_url": "{instance_url}/repos/{user}/{repo}/contents/{{+path}}",
            "compare_url": "{instance_url}/repos/{user}/{repo}/compare/{{base}}...{{head}}",
            "merges_url": "{instance_url}/repos/{user}/{repo}/merges",
            "archive_url": "{instance_url}/repos/{user}/{repo}{{/archive_format}}{{/ref}}",
            "downloads_url": "{instance_url}/repos/{user}/{repo}/downloads",
            "issues_url": "{instance_url}/repos/{user}/{repo}/issues{{/number}}",
            "pulls_url": "{instance_url}/repos/{user}/{repo}/pulls{{/number}}",
            "milestones_url": "{instance_url}/repos/{user}/{repo}/milestones{{/number}}",
            "notifications_url": "{instance_url}/repos/{user}/{repo}/notifications{{?since,all,participating}}",
            "labels_url": "{instance_url}/repos/{user}/{repo}/labels{{/name}}",
            "releases_url": "{instance_url}/repos/{user}/{repo}/releases{{/id}}",
            "deployments_url": "{instance_url}/repos/{user}/{repo}/deployments",
            "created_at": "1970-01-01T00:00:00Z",
            "updated_at": "1970-01-01T00:00:00Z",
            "pushed_at": "1970-01-01T00:00:00Z",
            "git_url": "git://github.com/{user}/{repo}.git",
            "ssh_url": "git@github.com:{user}/{repo}.git",
            "clone_url": "https://github.com/{user}/{repo}.git",
            "svn_url": "https://github.com/{user}/{repo}",
            "homepage": null,
            "size": 0,
            "stargazers_count": 0,
            "watchers_count": 0,
            "language": "Java",
            "has_issues": true,
            "has_projects": true,
            "has_downloads": true,
            "has_wiki": true,
            "has_pages": false,
            "has_discussions": false,
            "forks_count": 0,
            "mirror_url": null,
            "archived": false,
            "disabled": false,
            "open_issues_count": 1,
            "license": {{
                "key": "other",
                "name": "Other",
                "spdx_id": "NOASSERTION",
                "url": null,
                "node_id": "xxxxxxxxxx"
            }},
            "allow_forking": true,
            "is_template": false,
            "web_commit_signoff_required": false,
            "topics": [],
            "visibility": "public",
            "forks": 0,
            "open_issues": 1,
            "watchers": 0,
            "default_branch": "main",
            "temp_clone_token": null,
            "network_count": 0,
            "subscribers_count": 1
            }}"""

class GHAsset:
    def __init__(self, release, aid, name, content_type, size):
        self.release = release
        self.aid = aid
        self.name = name
        self.label = None
        self.content_type = content_type
        self.size = size

    def serialize(self):
        return f"""{{
            "url": "{instance_url}/repos/{self.release.user}/{self.release.repo}/releases/assets/{self.aid}",
            "id": {self.aid},
            "node_id": "xxxxxxxxxxxxxxxxx",
            "name": "{self.name}",
            "label": {self.label or "null"},
            "uploader": {{
                "login": "{self.release.user}",
                "id": 0,
                "node_id": "xxxxxxxxxxxx",
                "avatar_url": "",
                "gravatar_id": "",
                "url": "{instance_url}/users/{self.release.user}",
                "html_url": "{instance_url}/view/{self.release.user}",
                "followers_url": "{instance_url}/users/{self.release.user}/followers",
                "following_url": "{instance_url}/users/{self.release.user}/following{{/other_user}}",
                "gists_url": "{instance_url}/users/{self.release.user}/gists{{/gist_id}}",
                "starred_url": "{instance_url}/users/{self.release.user}/starred{{/owner}}{{/repo}}",
                "subscriptions_url": "{instance_url}m/users/{self.release.user}/subscriptions",
                "organizations_url": "{instance_url}/users/{self.release.user}/orgs",
                "repos_url": "{instance_url}/users/{self.release.user}/repos",
                "events_url": "{instance_url}/users/{self.release.user}/events{{/privacy}}",
                "received_events_url": "{instance_url}/users/{self.release.user}/received_events",
                "type": "User",
                "site_admin": false
            }},
            "content_type": "{self.content_type}",
            "state": "uploaded",
            "size": {self.size},
            "download_count": 0,
            "created_at": "1970-01-01T00:00:00Z",
            "updated_at": "1970-01-01T00:00:00Z",
            "browser_download_url": "{instance_url}/{self.release.user}/{self.release.repo}/releases/download/{self.release.tag}/{self.name}"
        }}"""


class GHRelease:
    def __init__(self, user, repo, rel_id, tag, name, message, commit, is_draft, is_prerelease):
        self.user = user
        self.repo = repo
        self.rel_id = rel_id
        self.tag = tag
        self.message = message
        self.name = name
        self.commit = commit
        self.is_draft = is_draft
        self.is_prerelease = is_prerelease
        self.assets = {}

    def serialize(self):
        return f"""{{
            "url": "{instance_url}/repos/{self.user}/{self.repo}/releases/{self.rel_id}",
            "assets_url": "{instance_url}/repos/{self.user}/{self.repo}/releases/{self.rel_id}/assets",
            "upload_url": "{instance_url}/repos/{self.user}/{self.repo}/releases/{self.rel_id}/assets{{?name,label}}",
            "html_url": "{instance_url}/view/{self.user}/{self.repo}/releases/tag/{self.tag}",
            "id": {self.rel_id},
            "author": {{
                "login": "{self.user}",
                "id": 0,
                "node_id": "xxxxxxxxxxxxxxxxxx",
                "avatar_url": "",
                "gravatar_id": "",
                "url": "{instance_url}/users/{self.user}",
                "html_url": "https://github.com/{self.user}",
                "followers_url": "{instance_url}/users/{self.user}/followers",
                "following_url": "{instance_url}/users/{self.user}/following/{{other_user}}",
                "gists_url": "{instance_url}/users/{self.user}/gists{{/gist_id}}",
                "starred_url": "{instance_url}/users/{self.user}/starred{{/owner}}{{/repo}}",
                "subscriptions_url": "{instance_url}/users/{self.user}/subscriptions",
                "organizations_url": "{instance_url}/users/{self.user}/orgs",
                "repos_url": "{instance_url}/users/{self.user}/repos",
                "events_url": "{instance_url}/users/{self.user}/events{{/privacy}}",
                "received_events_url": "{instance_url}/users/{self.user}/received_events",
                "type": "User",
                "site_admin": false
            }},
            "node_id": "xxxxxxxxxxxxxxxxxxxx",
            "tag_name": "{self.tag}",
            "target_commitish": "main",
            "name": "{self.name}",
            "draft": {"true" if self.is_draft else "false"},
            "prerelease": {"true" if self.is_prerelease else "false"},
            "created_at": "1970-01-01T00:00:00Z",
            "published_at": "1970-01-01T00:00:00Z",
            "assets": {self.serialize_assets()},
            "tarball_url": "{instance_url}/repos/{self.user}/{self.repo}/tarball/{self.tag}",
            "zipball_url": "{instance_url}/repos/{self.user}/{self.repo}/zipball/{self.tag}",
            "body": "{self.message}"
        }}"""

    def serialize_assets(self):
        return "[" + ",".join(map(lambda a: a.serialize(), self.assets.values())) + "]"

    def add_asset(self, name, ctype, size):
        asset_id = next_aid()
        asset = GHAsset(self, asset_id, name, ctype, size)
        self.assets[asset_id] = asset
        return asset

    @classmethod
    def from_json(cls, user, repo, rel_id, data):
        tag_name = data['tag_name']
        name = data.get('name', tag_name)
        message = data.get('body', tag_name)
        commit = data.get('target_commitish')
        is_draft = bool(data.get('draft', "false"))
        is_prerelease = bool(data.get('prerelease', "false"))
        return GHRelease(user, repo, rel_id, tag_name, name, message, commit, is_draft, is_prerelease)

release_map = {}
asset_map = {}

@app.route("/repos/<user>/<repo>/releases", methods=['POST'])
def e_release_create(user, repo):
    rel_id = len(release_map) * 400 + 1000000

    data = request.get_json()
    data['rel_id'] = rel_id

    rel = GHRelease.from_json(user, repo, rel_id, data)
    release_map[rel_id] = rel

    print(f"REL {rel.tag} / {rel.name} : {rel.message}")

    return rel.serialize()

@app.route("/repos/<user>/<repo>/releases", methods=['GET'])
def e_release_list(user, repo):
    return "[" + ",".join(map(lambda d: d.serialize(), release_map.values())) + "]"

@app.route("/repos/<user>/<repo>/releases/<rel_id>", methods=['GET'])
def e_release_get(user, repo, rel_id):
    rel_id = int(rel_id)
    if not rel_id in release_map:
        abort(404)
    return release_map[rel_id].serialize()


@app.route("/repos/<user>/<repo>/releases/<rel_id>/assets", methods=['GET'])
def e_assets_get(user, repo, rel_id):
    rel_id = int(rel_id)
    if not rel_id in release_map:
        abort(404)
    return release_map[rel_id].serialize_assets()

@app.route("/repos/<user>/<repo>/releases/<rel_id>/assets", methods=['POST'])
def e_assets_upload(user, repo, rel_id):
    rel_id = int(rel_id)
    if not rel_id in release_map:
        abort(404)

    rel = release_map[rel_id]
    a_name = request.args['name']
    asset = rel.add_asset(a_name, request.headers['Content-Type'], len(request.data))
    asset_map[asset.aid] = asset
    return asset.serialize()

@app.route("/repos/<user>/<repo>/releases/assets/<asset_id>", methods=['PATCH'])
def e_asset_label(user, repo, asset_id):
    asset_id = int(asset_id)
    if not asset_id in asset_map:
        abort(404)
    args = request.get_json()
    asset_map[asset_id].label = args['label']
    return ""

@app.route("/repos/<user>/<repo>/releases/assets/<asset_id>", methods=['GET'])
def e_asset_get(user, repo, asset_id):
    asset_id = int(asset_id)
    if not asset_id in asset_map:
        abort(404)
    return asset_map[asset_id].serialize()

if __name__ == "__main__":
    app.run(debug=False)
