# This is an example Dangerfile containing many useful examples of what can be done with Danger

# Make sure you read every comment (#) to enjoy proper Danger setup
# Every part of this setup is optional, so pick, remove, adjust and add as you need
# You can remove the comments afterwards

# To setup Danger with repo on Github.com you need to add DANGER_GITHUB_API_TOKEN for a user to your CI secret and expose it for PRs

# To setup Danger on enterprise Github, you need to also add these environment variables:
# DANGER_GITHUB_HOST=git.corp.goodcorp.com - host that the Github is running on
# DANGER_GITHUB_API_BASE_URL=https://git.corp.goodcorp.com/api/v3 - host at which the Github API is reachable on

# To setup Danger for Slack, you need to add SLACK_API_TOKEN to your CI secrets - a token for a bot user on Slack

require 'mobiledevops/plugin'

# Check env variable:
def envBlank?(env)
 return env.nil? || env.empty?
end

def pluginEnabled?(env)
  env.to_s.downcase == "true"
end

# Danger actions:

# Android Lint reporting:
unless envBlank?(ENV["LINT_REPORT_PATH"])
    android_lint.skip_gradle_task = true
    android_lint.filtering = true

    # Specify your exact report location
    android_lint.report_file = ENV["LINT_REPORT_PATH"]
    android_lint.lint(inline_mode: true)
end

if pluginEnabled?(ENV["DETEKT_ENABLED"])
    # Detekt reporting:
    kotlin_detekt.filtering = true
    # Skip default gradle task instead you should always use `detektAll` task which
    # supports multimodule setup and will run checks across whole codebase
    kotlin_detekt.skip_gradle_task = true

    # Specify your exact report location - for multi-module projects you should look at buildscript/detekt.gradle in NAT
    # A task called detektGenerateMergedReport in there will generate a single-file report for you
    kotlin_detekt.report_file = 'build/reports/detekt/detekt.xml'
    kotlin_detekt.detekt(inline_mode: true)
end

# JUnit test reporting:
if pluginEnabled?(ENV["JUNIT_ENABLED"])
    # JUnit just parses already existing reports
    junit_tests_dir = "**/test-results/**/*.xml"
    Dir[junit_tests_dir].each do |file_name|
        junit.parse file_name
        junit.report
    end
end

# Jacoco reporting:
unless envBlank?(ENV["JACOCO_REPORT_PATH"])
    # Uncomment to enforce minimum coverage of your choice, causing build fail when this is not met:
    #jacoco.minimum_project_coverage_percentage = 50
    #jacoco.minimum_class_coverage_percentage = 75

    # Specify your exact report location
    jacoco.report(ENV["JACOCO_REPORT_PATH"], fail_no_coverage_data_found: false)
end

# Jira link commenting (based on PR title or commits messages):
unless envBlank?(ENV["JIRA_IDENTIFIERS"]) || envBlank?(ENV["JIRA_SUBDOMAIN"])
    jira.check(
        key: ENV["JIRA_IDENTIFIERS"].split(","), #first part of your typical task identifier, like MOB-250
        url: "https://#{ENV["JIRA_SUBDOMAIN"]}.atlassian.net/browse", # put your jira subdomain in place of "netguru" or leave as is
        search_title: true,
        search_commits: false,
        fail_on_warning: false,
        report_missing: true,
        skippable: true # you can skip this check by putting [no-jira] in PR's title
    )
end

#Apk Analyzer
unless envBlank?(ENV["APK_PATH"])
    apkanalyzer.apk_file = ENV["APK_PATH"]
    apkanalyzer.file_size
    apkanalyzer.method_references
end

# Notifying selected slack channel about finished build:

# get link to jira ticket from raport
# @return [[String]]
def jira_link
  filteredMessages = status_report[:messages].grep(/atlassian/)
  jiraLink = nil
  unless filteredMessages.empty?
    jiraLink = filteredMessages.first[/(?<=href=')(.*atlassian.*)(?=')/]
  end
  jiraLink ? "Jira ticket: #{jiraLink}" : nil
end

# get code coverage of project
# @return [[String]]
def code_cov
  codeCov = nil
  status_report[:markdowns].each { |markdown|
    codeCov = markdown.message[/(?<=Coverage )(.*)(?= :)/]
    break unless codeCov.nil?
  }
  codeCov
end

# get status_report text for Slack
# @return [[String]]
def slack_report
  errors_count = status_report[:errors].count
  warnings_count = status_report[:warnings].count
  "There were #{errors_count} errors and #{warnings_count} warnings.\nCurrent code coverage: #{code_cov}.\n#{jira_link}"
end


emoji = [":rocket:", ":parrot:", ":fire:", ":hammer:"].sample
unless envBlank?(ENV["SLACK_NOTIFICATION_CHANNEL"])
    # Update channel to the one you want to notify
    slack.notify(channel: "##{ENV["SLACK_NOTIFICATION_CHANNEL"]}", text: "Hello, a build just finished! #{emoji}\n#{slack_report}\nFind it here: #{github.pr_json['html_url']}")
end

unless envBlank?(ENV["MOPS_PROJECT_ID"])
    issues = android_lint.send :read_issues_from_report
    android_lint.severity = "Error"
    errors = android_lint.send :filter_issues_by_severity, issues
    warnings = issues.length - errors.length
    coverage = code_cov.delete_suffix('%').to_i
    mobiledevops.request_url = ENV["MOPS_URL"]
    mobiledevops_json = { "project_id": ENV["MOPS_PROJECT_ID"], "code_coverage": coverage, "linter_result": { "errors": errors.length, "warnings": warnings } }
    mobiledevops.upload(mobiledevops_json)
end
