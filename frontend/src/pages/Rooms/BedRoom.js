import BedRoomContent from '../../components/BedRoomContent';
import RoomsLayout from '../../components/RoomsLayout';

export default function LivingRoom() {
    return <RoomsLayout roomName="Bed Room" ContentComponent={BedRoomContent} />;
};