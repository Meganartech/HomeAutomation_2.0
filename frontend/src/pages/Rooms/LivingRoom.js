import RoomsLayout from '../../components/RoomsLayout';
import LivingRoomContent from '../../components/LivingRoomContent';

export default function LivingRoom() {
    return <RoomsLayout roomName="Living Room" ContentComponent={LivingRoomContent} />;
};