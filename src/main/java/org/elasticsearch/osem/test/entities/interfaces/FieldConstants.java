/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.elasticsearch.osem.test.entities.interfaces;

/**
 *
 * @author andrewregan
 */
public interface FieldConstants
{
	String	ACTOR_ID			= "actorId";
	String	ACTOR_FULL_NAME_LCASE		= "actorFullName";
	String	ACTOR_FULL_NAME			= "actorFullNameOrigCase";
	String	ACTOR_FORENAMES			= "forenames";
	String	ACTOR_SURNAME			= "surname";
	String	ACTOR_USERNAME			= "userName";
	String	ACTOR_TWITTER_ACCOUNT		= "actorTwitterAcctName";

	String	TARGET_ACTOR_USERNAME		= "targetActorUsername";
	String	TARGET_RESOURCE_ID		= "targetResId";
	String	TARGET_RESOURCE_TYPE		= "targetResType";
	String	TARGET_RESOURCE_DISPLAY_NAME	= "targetResDispName";

	String	ARTICLE_ID			= "articleId";
	String	ARTICLE_AUTHOR_USERNAME		= "articleAuthorsUserName";
	String	ARTICLE_PUB_DATE_MSECS		= "articlePubDateMSecs";
	String	ARTICLE_FROM_GROUP_RESOURCE	= "articleFromGroupRes";
	String	ARTICLE_NUM_POTENTIAL_AUTHORS	= "articleNumPtlAuthors";
	String	ARTICLE_RES_DISPLAY_NAME	= "articleResName";
	String	ARTICLE_CATEGORIES_COLL		= "articleCategories";
	String	ARTICLE_TOPIC_NAME_COLL		= "articleTopicNames";
	String	ARTICLE_SEMANTICALLY_ANALYSED	= "semanticallyAnalysed";
	String	ARTICLE_SEM_TOPIC_SENTIMENTS	= "articleTopicSentiments";
	String	ARTICLE_AGGREGATION_TIME	= "articleAggTimeMS";
	String	ARTICLE_READ_COUNT		= "articleReadCount";
	String	ARTICLE_FAVES_COUNT		= "articleFavesCount";
	String	ARTICLE_FLAGS_COUNT		= "articleFlagsCount";
	String	ARTICLE_ASS_FLAGS_COUNT		= "articleAssertonFlagsCount";
	String	ARTICLE_LINK_URLS		= "articleLinkURLs";
	String	ARTICLE_TWITTER_HAS_HASHTAGS	= "articleTwitterHasTags";
	String	ARTICLE_TWITTER_HASHTAGS	= "articleTwitterHashtags";
	String	ARTICLE_TWITTER_MENTIONS	= "articleTwitterMentions";
	String	ARTICLE_TWITTER_REPLYTO		= "articleTwitterReplyTo";
	String	ARTICLE_TWITTER_MSGTYPE		= "articleTwitterMsgType";

	String	WIKI_ARTICLE_DOWNLOAD_TIME	= "articleDownloadTimeMS";

	String	INDEX_ID_COLUMN			= ARTICLE_ID;
	String	INDEX_DATE_MSECS_COLUMN		= "activityItemDateMSecs";
	String	INDEX_URL_COLUMN		= "articleURL";
	String	INDEX_TITLE_COLUMN		= "articleTitle";
	String	INDEX_CONTENT_COLUMN		= "content";

	String	CATEGORY_NAME			= "categoryName";
	String	LCASE_CATEGORY_NAME		= "lowercaseCatName";

	String	TAG_NAME			= "tagName";
	String	TAG_NAME_LOWERCASE		= "lowercaseTagName";

	String	GROUP_ID			= "groupId";
	String	GROUP_NICKNAME			= "groupNickname";
	String	GROUP_DISPLAY_NAME		= "groupDisplayName";
	String	GROUP_DESCRIPTION		= "groupDesc";
	String	GROUP_MEMBERSHIP_POLICY		= "groupMbrsPolicy";
	String	GROUP_MEMBERS_COUNT		= "groupMbrsCount";
	String	GROUP_TWITTER_ACCOUNT		= "groupTwitterAcctName";
	String	GROUP_INFO_JSON			= "groupInfoJSON";
	String	GROUP_ISO_3166_CODE		= "groupIso3166Code";

	String	FLAG_ID				= "flagId";
	String	FLAG_COMMENT			= "flagComment";
	String	FLAGGED_ARTICLE_ID		= "flaggedArticleId";
	String	FLAGGED_ARTICLE_TITLE		= "flaggedArticleTitle";
	String	FLAGGED_ARTICLE_FRAG		= "flaggedArticleFrag";
	String	FLAGGED_ARTICLE_URL		= "flaggedOrigURL";

	String	ARTICLE_VERSION_ID		= "articleVersionId";

	String	ARTICLE_RATING_ID		= "articleRatingId";

	String	RELATIONSHIP_ID			= "relnId";
	String	RELATIONSHIP_TARGET_USERNAME	= TARGET_ACTOR_USERNAME;
	String	RELATIONSHIP_TARGET_GROUP	= "targetGroupDisplayName";
	String	RELATIONSHIP_TARGET_GROUP_ID	= "targetGroupId";
	String	RELATIONSHIP_TARGET_BLOG	= "targetBlogDisplayName";
	String	RELATIONSHIP_TARGET_BLOG_ID	= "targetBlogId";
	String	RELATIONSHIP_TARGET_FEED	= "targetFeedDisplayName";
	String	RELATIONSHIP_TARGET_FEED_ID	= "targetFeedId";
	String	RELATIONSHIP_TARGET_ZONE_ID	= "targetZoneId";

	String	TERM_NAME			= "termName";

	String	BLOG_ID				= "blogId";
	String	BLOG_DISPLAY_NAME		= "blogDisplayName";
	String	BLOG_URL			= "blogURL";
	String	BLOG_URL_TERMS			= "blogURLTerms";
	String	BLOG_IS_GROUP			= "blogIsGroup";
	String	BLOG_LOCALE			= "blogLocale";
	String	BLOG_MEMBERS_COUNT		= "blogMbrsCount";
	String	BLOG_TWITTER_ACCOUNT		= "blogTwitterAcctName";
	String	BLOG_MULTI_AUTHORED		= "blogMultiAuthored";

	String	FEED_ID				= "feedId";
	String	FEED_DISPLAY_NAME		= "feedDisplayName";
	String	FEED_URL			= "feedURL";
	String	FEED_URL_TERMS			= "feedURLTerms";
	String	FEED_DESCRIPTION		= "feedDescription";
	String	FEED_LOCALE			= "locale";
	String	FEED_IS_GROUP			= "feedIsGroup";
	String	FEED_MEMBERS_COUNT		= "feedMbrsCount";
	String	FEED_TWITTER_ACCOUNT		= "feedTwitterAcctName";

	String	WIKI_ID				= "wikiId";
	String	WIKI_DISPLAY_NAME		= "wikiDisplayName";
	String	WIKI_URL			= "wikiURL";
	String	WIKI_URL_TERMS			= "wikiURLTerms";
	String	WIKI_BASE_URL			= "wikiBaseURL";
	String	WIKI_LOCALE			= "wikiLocale";

	String	CUSTOM_FEED_ID			= "customFeedId";
	String	CUSTOM_FEED_NAME		= "customFeedName";
	String	CUSTOM_FEED_DESCRIPTION		= "customFeedDesc";

	String	RECOMMENDATIONS_FEED_ID		= "recFeedId";

	String	JURISDICTION_ID			= "jurisdictionId";
	String	JURISDICTION_NAME_KEY		= "jurisdictionName";
	String	JURISDICTION_DESCRIPTION	= "jurisdictionDescription";

//	String	ASSERT_ID			= "assertId";
	String	ASSERT_SNIPPET			= "assertContent";
	String	ASSERT_FLAGGEE_USERNAMES	= "assertUsernames";

	String	SRC_CITATION_ID			= "citationId";

	String	RESPONSE_ID			= "responseId";
	String	RESPONSE_CONTENT		= "responseContent";
	String	RESPONSE_FLAGGEE_USERNAMES	= "responseUsernames";

	String	RESPONSE_RATING_ID		= "respRatingId";
	String	RESPONSE_RATING_VALUE		= "respRatingValue";
	String	RESPONSE_RATING_FLAGGEE_UNAMES	= "respRatingUsernames";

	String	TOPIC_RESULT_ID			= "topic_ResultId";
	String	TOPIC_NAME			= "topicName";
	String	TOPIC_WEIGHT			= "topicWeight";
	String	TOPIC_TYPES_VALUE		= "topicTypesValue";
	String	TOPIC_TYPES_NAME		= "topicTypesName";

	String	BLOG_CACHE_ID			= BLOG_ID;
	String	BLOG_CACHE_DISPLAY_NAME		= BLOG_DISPLAY_NAME;
	String	BLOG_CACHE_CONTENT		= "blogCacheContent";
	String	BLOG_CACHE_ANALYZER		= "blogCacheAnalyzer";
	String	BLOG_CACHE_TOPIC_NAME		= "blogCacheTopic";
	String	BLOG_CACHE_GROUP_INFO		= "blogCacheGroupInfo";

	String	INCOMING_LINKS_CACHE_ID				= "ilcId";
	String	INCOMING_LINKS_CACHE_RES_TYPE			= "ilcResType";
	String	INCOMING_LINKS_CACHE_RES_ID			= "ilcResId";
	String	INCOMING_LINKS_CACHE_DISP_NAME			= "ilcDisplayName";
	String	INCOMING_LINKS_CACHE_ACTOR_ID			= "ilcActorId";
	String	INCOMING_LINKS_CACHE_ARTICLE_COUNT		= "ilcArticleCount";
	String	INCOMING_LINKS_CACHE_SOURCE_ARTICLE_ID		= "ilcSrcArticleId";
	String	INCOMING_LINKS_CACHE_SOURCE_ARTICLE_TITLE	= "ilcSrcArticleTitle";
	String	INCOMING_LINKS_CACHE_SOURCE_ARTICLE_TIME	= "ilcSrcArticleTime";
	String	INCOMING_LINKS_CACHE_METADATA			= "ilcSrcMetadata";
	String	INCOMING_LINKS_CACHE_LINKWEIGHT_SCORE		= "ilcSrcScore";

	String	INCOMING_LINKS_SCORES_ID			= "ilScoresId";
	String	INCOMING_LINKS_SCORES_SCORE			= "ilScoresScore";
	String	INCOMING_LINKS_SCORES_ORDER			= "ilScoresOrder";

	String	GOOGLE_PAGE_RANK_CACHE_ID	= "pageRankId";
	String	GOOGLE_PAGE_RANK_CACHE_SCORE	= "pageRankScore";
	String	GOOGLE_PAGE_RANK_CACHE_TIME_MS	= "pageRankTimeMS";

	String	GOOGLE_MODERATOR_ENTRY_ID			= "gmeId";
	String	GOOGLE_MODERATOR_ENTRY_SERIES_ID		= "gmeSeriesId";
	String	GOOGLE_MODERATOR_ENTRY_TOPIC_ID			= "gmeTopicId";
	String	GOOGLE_MODERATOR_ENTRY_SUBMISSION_ID		= "gmeSubmissionId";
	String	GOOGLE_MODERATOR_ENTRY_CONTENT			= INDEX_CONTENT_COLUMN;
	String	GOOGLE_MODERATOR_ENTRY_REFERRED_ARTICLE_ID	= "gmeRefArticleId";
	String	GOOGLE_MODERATOR_ENTRY_TIME_MS			= "gmeTimeMS";

	String	EDITORIAL_CITATIONS_ID			= "editorialAC_Id";
	String	EDITORIAL_CITATIONS_ACTOR_IDS		= "editorialAC_ActorIds";
	String	EDITORIAL_CITATIONS_BLOG_IDS		= "editorialAC_BlogIds";
	String	EDITORIAL_CITATIONS_FEED_IDS		= "editorialAC_FeedIds";
	String	EDITORIAL_CITATIONS_GROUP_IDS		= "editorialAC_GroupIds";
	String	EDITORIAL_CITATIONS_ZONE_IDS		= "editorialAC_ZoneIds";
	String	EDITORIAL_CITATIONS_FRAGMENT		= "editorialAC_Frag";
	String	EDITORIAL_CITATIONS_LINKEE_INFO_JSON	= "editorialAC_LinkeeInfo";
}