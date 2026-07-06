UPDATE match_edges
SET score = LEAST(100, ROUND(score * 100.0 / (
    95
    + CASE WHEN COALESCE(TRIM((SELECT sub_category FROM provide_items WHERE id = match_edges.provide_item_id)), '') <> ''
            AND COALESCE(TRIM((SELECT sub_category FROM want_items WHERE id = match_edges.want_item_id)), '') <> ''
        THEN 25 ELSE 0 END
    + CASE WHEN (SELECT estimated_value FROM provide_items WHERE id = match_edges.provide_item_id) IS NOT NULL
            AND (SELECT min_value FROM want_items WHERE id = match_edges.want_item_id) IS NOT NULL
            AND (SELECT max_value FROM want_items WHERE id = match_edges.want_item_id) IS NOT NULL
        THEN 20 ELSE 0 END
)));

UPDATE match_candidates
SET score = COALESCE((
    SELECT ROUND(AVG(match_edges.score))
    FROM match_edges
    WHERE match_edges.match_candidate_id = match_candidates.id
), score);
