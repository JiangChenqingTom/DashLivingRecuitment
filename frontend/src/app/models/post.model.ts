export interface Post {
    id: number;
    title: string;
    content: string;
    authorId: number;
    authorUsername: string;
    createdAt: string;
    updatedAt: string;
    viewCount: number;
    commentCount: number;
    published: boolean;
}

export interface PostComment {
    id: number;
    content: string;
    postId: number;
    userId: number;
    username: string;
    createdAt: string;
    updatedAt: string;
    parentId: number | null;
    replies: PostComment[];
}

export interface PageResponse<T> {
    content: T[];
    pageable: {
        sort: {
            empty: boolean;
            sorted: boolean;
            unsorted: boolean;
        };
        offset: number;
        pageNumber: number;
        pageSize: number;
        paged: boolean;
        unpaged: boolean;
    };
    totalPages: number;
    totalElements: number;
    last: boolean;
    size: number;
    number: number;
    sort: {
        empty: boolean;
        sorted: boolean;
        unsorted: boolean;
    };
    first: boolean;
    numberOfElements: number;
    empty: boolean;
}
