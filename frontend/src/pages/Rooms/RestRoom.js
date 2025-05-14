import RestRoomContent from '../../components/RestRoomContent';
import RoomsLayout from '../../components/RoomsLayout';

export default function LivingRoom() {
    return <RoomsLayout roomName="RestRoom" ContentComponent={RestRoomContent} />;
};