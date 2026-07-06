INSERT INTO trade_rooms (match_candidate_id, status, created_at)
SELECT candidate.id, 'IN_EXCHANGE', candidate.created_at
FROM match_candidates candidate
WHERE candidate.status = 'ACCEPTED'
  AND NOT EXISTS (
    SELECT 1 FROM trade_rooms room WHERE room.match_candidate_id = candidate.id
  );

INSERT INTO trade_room_members (trade_room_id, user_id, last_read_message_id, completed_at)
SELECT room.id, participant.user_id, NULL, NULL
FROM trade_rooms room
JOIN match_participants participant ON participant.match_candidate_id = room.match_candidate_id
WHERE NOT EXISTS (
    SELECT 1 FROM trade_room_members member
    WHERE member.trade_room_id = room.id AND member.user_id = participant.user_id
);
