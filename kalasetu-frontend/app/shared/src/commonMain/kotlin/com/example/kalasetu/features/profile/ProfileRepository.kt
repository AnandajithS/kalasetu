package com.example.kalasetu.features.profile

open class ProfileRepository(
    private val initialProfile: Profile? = null
) : ProfileRepositoryContract {
    override suspend fun fetchProfile(userId: String): Profile {
        val baseProfile = Profile(
            id = userId,
            name = "Sarah Anderson",
            username = "sarahart",
            location = "San Francisco, CA",
            bio = "Digital artist & illustrator passionate about creating vibrant character designs and exploring new visual narratives. Available for commissions and collaborations.",
            avatarUrl = null,
            //followers = 2847,
            //following = 892,
            //artworksCount = 156,
            //totalLikes = 12400,
            email = "sarah.anderson@email.com",
            skills = listOf(
                "Digital Art", "Illustration", "Character Design",
                "Concept Art", "Storyboarding", "Visual Development",
                "Graphic Design", "UI/UX", "Animation"
            ),
            artworksImages = listOf(
                "https://example.com/art1.jpg",
                "https://example.com/art2.jpg",
                "https://example.com/art3.jpg"
            ),
            achievements = listOf(
                Achievement(" Top Creator 2024", "Recognized as top 1% creator"),
                Achievement(" 1K Followers", "Reached 1000 followers milestone"),
                Achievement(" Featured Artist", "Featured in monthly showcase")
            )
        )

        return if (initialProfile != null) {
            baseProfile.copy(
                name = initialProfile.name,
                username = initialProfile.username,
                location = initialProfile.location,
                bio = initialProfile.bio,
                email = initialProfile.email,
                avatarUrl = initialProfile.avatarUrl,
                avatarBytes = initialProfile.avatarBytes
            )
        } else {
            baseProfile
        }
    }
}